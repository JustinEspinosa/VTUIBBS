package vtui.bbs.util.domain;

import gnu.crypto.cipher.CipherFactory;
import gnu.crypto.cipher.IBlockCipher;
import gnu.crypto.mac.IMac;
import gnu.crypto.mac.HMacFactory;
import gnu.crypto.mode.IMode;
import gnu.crypto.mode.ModeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.security.sasl.SaslClient;

import textmode.curses.net.GeneralSocketIO;
import textmode.curses.net.SocketIO;
import textmode.util.Base64;
import textmode.xfer.util.Arrays;
import textmode.xfer.util.HexBuffer;
import textmode.xfer.util.Arrays.Endianness;



/**
 *  
 * @author justin
 *
 */
public class ApplePasswordService {
	
	private class CASTInputStream extends InputStream{

		private IMode castCBCIn;
		private byte[] buffer = null;
		private int    bpos;
		
		public CASTInputStream(){
			IBlockCipher castCipher = CipherFactory.getInstance("cast5");
			castCBCIn = ModeFactory.getInstance("cbc", castCipher, 8);
			
			Map<String,Object> attributes = new HashMap<String,Object>();
			attributes.put(IMode.MODE_BLOCK_SIZE, new Integer(8));
			attributes.put(IMode.IV, new byte[]{0,0,0,0,0,0,0,0});
			attributes.put(IMode.STATE, new Integer(IMode.DECRYPTION));
			attributes.put(IBlockCipher.CIPHER_BLOCK_SIZE, new Integer(8));
		    attributes.put(IBlockCipher.KEY_MATERIAL, castKey);
		    
			try {
				castCBCIn.init(attributes);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			

			buffer = new byte[castCBCIn.currentBlockSize()];
			bpos   = buffer.length;
		}
		
		private boolean readMore() throws IOException{
			byte[] inBuff = new byte[castCBCIn.currentBlockSize()];
			int rb = 0,p = 0;
			
			while( p<inBuff.length && (rb=ioObj.getInputStream().read()) != -1 )
				inBuff[p++] = (byte)rb;

			//System.out.println("InEnc");
			//dumpBytes(inBuff);
			
			if(p<inBuff.length)
				return false;
			
			castCBCIn.decryptBlock(inBuff, 0, buffer, 0);
			bpos = 0;
			
			//System.out.println("In");
			//dumpBytes(buffer);
			return true;
		}
		
		private int consume() throws IOException {
			if(bpos>=buffer.length)
				if(!readMore())
					return -1;
			
			return buffer[bpos++];
		}
		
		
		@Override
		public int read() throws IOException {
			return consume();
		}
		
	}
	private class CASTOutputStream extends OutputStream {


		private IMode castCBCOut;
		private byte[] buffer;
		private int    bpos   = 0;
		
		public CASTOutputStream(){
			IBlockCipher castCipher = CipherFactory.getInstance("cast5");
			castCBCOut = ModeFactory.getInstance("cbc", castCipher, 8);
			
			Map<String,Object> attributes = new HashMap<String,Object>();
			attributes.put(IMode.MODE_BLOCK_SIZE, new Integer(8));
			attributes.put(IMode.IV, new byte[]{0,0,0,0,0,0,0,0});
			attributes.put(IMode.STATE, new Integer(IMode.ENCRYPTION));
			attributes.put(IBlockCipher.CIPHER_BLOCK_SIZE, new Integer(8));
		    attributes.put(IBlockCipher.KEY_MATERIAL, castKey);
		    
			try {
				castCBCOut.init(attributes);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			

			buffer = new byte[castCBCOut.currentBlockSize()];
			bpos   = 0;
		}
		
		@Override
		public void write(int b) throws IOException {
			
			buffer[bpos++] = (byte)b;
			
			if(bpos>=buffer.length)
				encryptAndWrite();

		}

		private void encryptAndWrite() throws IOException {

			//System.out.println("Out");
			//dumpBytes(buffer);
			
			byte[] outBuff = new byte[buffer.length];
			castCBCOut.encryptBlock(buffer,0,outBuff,0);
			bpos = 0;
			
			//System.out.println("Outenc");
			//dumpBytes(outBuff);
			
			ioObj.getOutputStream().write(outBuff);
			ioObj.getOutputStream().flush();
		}
		
		public void pad(){
			while(bpos<buffer.length)
				buffer[bpos++]=0;
		}

		@Override
		public void flush() throws IOException {
			 pad();
			 encryptAndWrite();
		}
	}
	
	private InputStream in;
	private OutputStream out;
	
	private String passwordSrvHost;
	private int passwordSrvPort;
	private SocketChannel clientSock;
	private SocketIO ioObj;
	private RSAPublicKey serverKey;
	private byte[] castKey;
	private KeyFactory factory;
	
	BigInteger modulus;
	
	public ApplePasswordService(String hostname,int port){
		passwordSrvHost = hostname;
		passwordSrvPort = port;
		
		//System.err.println("apple-sasl -> "+hostname+":"+port);
		
		try {
			factory = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		
	}
	
	public void disconnect(){
		try {
			write("QUIT");
			clientSock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void cleanUpIn(){
		/*
		if(in instanceof CASTInputStream)
			((CASTInputStream) in).discard();
			*/
	}
	
	private void enableCast() throws IOException{
		in =  new CASTInputStream();
		out = new CASTOutputStream();
	}
	
	private void disableCast() throws IOException{
		in = ioObj.getInputStream();
		out = ioObj.getOutputStream();
	}
	
	public boolean connect(){
		try {
			clientSock = SocketChannel.open();
			if(!clientSock.connect(new InetSocketAddress(passwordSrvHost, passwordSrvPort)))
				return false;
			
			ioObj = new GeneralSocketIO(clientSock);	
			disableCast();
			
			return isOk(splitResult(read()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
		
	}
	

	private void appendArg(StringBuilder builder,String arg){
		if(arg!=null){
			builder.append(' ');
			builder.append(arg);
		}
	}

	private String buildCommand(String command,String ... args){
		StringBuilder builder = new StringBuilder(command);
		for(String arg:args)
			appendArg(builder,arg);
				
		builder.append((char)0x0d);
		builder.append((char)0x0a);
		
		return builder.toString();
	}
	
	private int decodePublicKey(String message){
		String[] messageParts = message.split(" ");
		BigInteger exponent = new BigInteger(messageParts[1]);
		modulus = new BigInteger(messageParts[2]);
		
		try {
			serverKey = (RSAPublicKey)factory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		return new Integer(messageParts[0]);
		
	}
	
	private byte[] RSADo(byte[] src, Cipher cipher,int inBlkSize,int outBlkSize) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException{
		int rest = (src.length % inBlkSize);
		int numBlocks = ((src.length-rest)/inBlkSize)+1;
		
		byte[] result = new byte[outBlkSize*numBlocks];
		for(int i=0;i<result.length;++i)
			result[i] = 0;
		
		int i, offsetOut = 0, offsetIn = 0;
		for(i=0;i<numBlocks-1;++i){
			offsetOut += cipher.update(src, offsetIn, inBlkSize, result, offsetOut);
			offsetIn  += inBlkSize;
		}
				
		cipher.doFinal(src, offsetIn,src.length-offsetIn,result, offsetOut);

		return result;
	}
	
	
	private byte[] RSAEncode(byte[] src){
		Cipher cipher;
		try {
			
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, (Key) serverKey);
			int inBlkSize = 117, outBlkSize = 128;

			return RSADo(src, cipher, inBlkSize,outBlkSize);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (ShortBufferException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	private String read() throws IOException{
		
		int c = 0; boolean gotnl = false;
		StringBuilder builder = new StringBuilder();

		while(!gotnl && ((c=in.read())!=-1)){
			switch(c){
			case '\n': /*System.out.println("<LF>");*/gotnl = true; break;
			case '\0': /*System.out.println("<NULL>");*/break;
			case '\r': /*System.out.println("<CR>");*/break;
			default  : /*System.out.printf("%02x",(byte)c);*/builder.append((char)c);
			}
		}
		cleanUpIn();
		return builder.toString();
	}
	
	private String[] splitResult(String resp){
		//System.err.println(resp);
		int r = resp.indexOf(' ');
		return new String[]{resp.substring(0,r),
							resp.substring(r+1)};
	}
	
	private boolean isOk(String ... resp){
		return resp[0].startsWith("+");
	}
	
	private void write(String cmd) throws IOException{
		out.write(cmd.getBytes());
		out.flush();
	}
	
	private String writeRead(String cmd) throws IOException{
		//System.err.println(cmd);
		write(cmd);
		return read();
	}
	
	private byte[] base64WithLen(String str){
		int start = str.indexOf("{")+1, end = str.indexOf("}");
		int len = Integer.parseInt(str.substring(start,end));
		
		return Arrays.copyOf(Base64.decode(str.substring(end+1)), len);
	}
	
	private String base64WithLen(byte[] bytes){
		StringBuilder data=new StringBuilder();
		data.append('{');
		data.append(bytes.length);
		data.append('}');
		data.append(Base64.encode(bytes));
		return data.toString();
	}
	
	private byte[] getRandom(){
		
		Random rnd = new SecureRandom(Arrays.fromLong((new Date()).getTime(),Endianness.Little));
		byte[] rndb = new byte[16];
		rnd.nextBytes(rndb);
		return rndb;
		
	}

	private boolean readPublicKey() throws IOException{
		String response = writeRead(buildCommand("RSAPUBLIC"));
		String[] decodedResp = splitResult(response);
		if(!isOk(decodedResp))
			return false;
		
		decodePublicKey(decodedResp[1]);
		
		return true;
	}
	
	private boolean sendCastKey() throws IOException{
		//prepare and send RSAVALIDATE
		byte[] rndNum = getRandom();
		StringBuilder rnd = new StringBuilder(new String(rndNum));
		rnd.append("hash");
		rnd.append('\0');
		String rndStr = rnd.toString();
		
		String response = writeRead(buildCommand("RSAVALIDATE",base64WithLen(RSAEncode(rndStr.getBytes()))));
		String[] decodedResp = splitResult(response);
		if(!isOk(decodedResp))
			return false;
		
		byte[] rndHashServer = base64WithLen(decodedResp[1]);
		byte[] rndHashMine = new byte[0];
		try{
			rndHashMine = MessageDigest.getInstance("MD5").digest(rndNum);
		}catch (NoSuchAlgorithmException e) {}
		
			
		if(Arrays.equals(rndHashServer, rndHashMine)){
			castKey = rndNum;
			enableCast();
			
			return true;
		}
		return false;		
	}
	
	public boolean beginCASTEncryption(){
		
		try {
			
			if(readPublicKey()){
				if(!sendCastKey())
					return sendCastKey();
				else
					return true;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return false;
	}
	
	public String list(){
		try {
			String[] response = splitResult(writeRead(buildCommand("LIST")));
			if(!response[0].equals("+OK"))
				return null;
			
			return response[1];
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String hexString(byte[] bin){
		//dumpBytes(bin);
		byte[] hex = HexBuffer.binToHex(bin);
		String sHex = new String(hex);
		//System.out.println(sHex);
		return sHex.toUpperCase();
	}
	
	private byte[] unHex(String str){
		byte[] hex = str.toLowerCase().getBytes();
		return HexBuffer.hexToBin(hex);
	}
	
	private String unHexString(String str){
		return new String(unHex(str));
	}
	
	private String startAuth(String username,String mech, String initial) throws IOException{
		String response = writeRead(buildCommand("USER",username,"AUTH",mech,initial));

		String[] rsplit = splitResult(response);
		if(rsplit[0].equals("+AUTHOK"))
			return rsplit[1];
		
		return null;
	}
	
	private String continueAuth(byte[] authData) throws IOException{
		String response = writeRead(buildCommand("AUTH2",hexString(authData)));
		
		String[] rsplit = splitResult(response);
		if(rsplit[0].equals("+OK"))
			return rsplit[1];
		
		return null;
	}
	
	public String getIdByName(String username){
		String cmd = buildCommand("GETIDBYNAME",username);
		
		try {
			String response = writeRead(cmd);
			String[] rslt = splitResult(response);
			if( rslt[0].startsWith("+OK") )
				return rslt[1];
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean authSASL(SaslClient client, String username){
		try{
			String initial=null;
			if(client.hasInitialResponse())
				initial=hexString(client.evaluateChallenge(new byte[0]));
			
			String challenge = startAuth(username, client.getMechanismName(), initial);
			if(challenge==null)
				return false;
			
			while(!client.isComplete()){
				byte[] binChallenge = unHex(challenge);
				//System.out.println(binChallenge.length);
				byte[] rsasl = client.evaluateChallenge(binChallenge);

				//System.out.println(new String(rsasl));
				if(rsasl!=null){
					challenge = continueAuth(rsasl);
					if(challenge==null)
						return false;
				}else{
					return (challenge!=null);
				}
			}
			return true;
		}catch(IOException e){
			e.printStackTrace();
		} 
		
		return false;
	}
	
	public boolean authDHX(String slotid,String password){
		try{			
			//APPLE DHX
			
	        PWServiceDHX dhx = new PWServiceDHX(slotid);
			String challenge = startAuth(slotid,"DHX",null);
			if(challenge==null)
				return false;
			
			challenge = continueAuth(dhx.clientStep1().getBytes());
			if(challenge==null)
				return false;

			try {
				challenge = continueAuth(dhx.clientStep2(challenge,password).getBytes());
				if(challenge==null)
					return false;
			} catch (InvalidKeyException e) {
				e.printStackTrace();
				return false;
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return false;
			}

			return true;
	
		}catch(IOException e){
			e.printStackTrace();
		}
		return false;

	}
	
	public boolean authDigestMd5(String userName,String password){
		try{
			//DIGEST-MD5
			

			String challenge = startAuth(userName,"DIGEST-MD5",null);
			if(challenge==null)
				return false;
		
			String cnonce = DigestMd5.generateCNonce();
			
			DigestMd5 algo = new DigestMd5(unHexString(challenge));
			algo.prepare(userName, password, passwordSrvHost,"rcmd", cnonce);
			String outChallenge = algo.generate();
			challenge = continueAuth(outChallenge.getBytes());
			//should check that challenge value but currently, I don't care...
			return (challenge!=null);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private byte[] toCramSecret(String str){
		if(str.length()>64)
			try {
				str = new String(MessageDigest.getInstance("MD5").digest(str.getBytes()));
			} catch (NoSuchAlgorithmException e) {}
		
		byte[] arr = new byte[64];
		System.arraycopy(str.getBytes(),0, arr, 0, str.length());
		return arr;
	}

	public boolean authCramMd5(String userName,String password){
		try{

			String challenge = startAuth(userName,"CRAM-MD5",null);
			if(challenge==null)
				return false;
									
			IMac macMd5 = HMacFactory.getInstance("hmac-md5");
			
			Map<String,Object> attributes = new HashMap<String, Object>();
			attributes.put(IMac.MAC_KEY_MATERIAL , toCramSecret(password) );
			
			macMd5.init(attributes);
			byte[] binchal = unHex(challenge);
			macMd5.update(binchal,0,binchal.length);
					
			byte[] mac = macMd5.digest();
			
			StringBuilder builder = new StringBuilder(userName);
			builder.append(' ');
			builder.append(new String(HexBuffer.binToHex(mac)));
			
			challenge = continueAuth(builder.toString().getBytes());
			return (challenge!=null);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Command to change the password:
	 * CHANGEPASS username datab64
	 * data is:
	 * int32 usernamelen
	 * utf8[] username
	 * int32 oldpwdlen
	 * utf8[] oldpwd
	 * int32 newpwdlen
	 * utf8[] newpwd
	 */
	public boolean changepass(String userName,String password){
		
		String cmd = buildCommand("CHANGEPASS",userName,password);
		
		try {
			String response = writeRead(cmd);
			String[] rslt = splitResult(response);
			return rslt[0].startsWith("+OK");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
