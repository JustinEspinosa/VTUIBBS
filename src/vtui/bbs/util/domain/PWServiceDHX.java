package vtui.bbs.util.domain;

import gnu.crypto.cipher.CipherFactory;
import gnu.crypto.cipher.IBlockCipher;
import gnu.crypto.mode.IMode;
import gnu.crypto.mode.ModeFactory;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import textmode.xfer.util.HexBuffer;

public class PWServiceDHX {


	//apple DHX crypto constants
	private static String header = "HDSD";
	private static byte[] clIV = "LWallace".getBytes();
	private static byte[] srIV = "CJalbert".getBytes();
	private static BigInteger p = UnsignedBIConverter.fromByteArray(new byte[]{
		    (byte)0xd9, (byte)0xc8, (byte)0xff, (byte)0xb9, (byte)0x1d, (byte)0xff, (byte)0x2f, (byte)0x94,
		    (byte)0xbf, (byte)0xd2, (byte)0xbe, (byte)0x97, (byte)0x42, (byte)0xde, (byte)0xea, (byte)0xbb,
		    (byte)0x8b, (byte)0x71, (byte)0xc0, (byte)0x51, (byte)0xe3, (byte)0x1e, (byte)0x39, (byte)0x76,
		    (byte)0xb9, (byte)0x72, (byte)0xb4, (byte)0x14, (byte)0x90, (byte)0x5b, (byte)0x1e, (byte)0x76,
		    (byte)0x88, (byte)0xd3, (byte)0x71, (byte)0x3d, (byte)0x5f, (byte)0x8f, (byte)0xb3, (byte)0xbd,
		    (byte)0x37, (byte)0x32, (byte)0x3f, (byte)0xa1, (byte)0x68, (byte)0xa5, (byte)0xea, (byte)0x54,
		    (byte)0xe4, (byte)0xcd, (byte)0xb7, (byte)0x30, (byte)0x8b, (byte)0x3f, (byte)0x2e, (byte)0xff,
		    (byte)0x43, (byte)0x7c, (byte)0x66, (byte)0xcb, (byte)0xac, (byte)0x0a, (byte)0xb8, (byte)0x1c,
		    (byte)0xcc, (byte)0x49, (byte)0xf3, (byte)0xb2, (byte)0x97, (byte)0x1c, (byte)0x2c, (byte)0x1d,
		    (byte)0x06, (byte)0x00, (byte)0xdb, (byte)0x47, (byte)0x9f, (byte)0xb9, (byte)0x7e, (byte)0xcf,
		    (byte)0x4e, (byte)0x71, (byte)0x07, (byte)0xe2, (byte)0x52, (byte)0xc3, (byte)0x43, (byte)0xb4,
		    (byte)0xef, (byte)0x21, (byte)0xf1, (byte)0x5f, (byte)0xf7, (byte)0x13, (byte)0x87, (byte)0x69,
		    (byte)0x29, (byte)0x28, (byte)0xa1, (byte)0xec, (byte)0x38, (byte)0xc1, (byte)0xe3, (byte)0xf9,
		    (byte)0x20, (byte)0x0b, (byte)0x9d, (byte)0x2b, (byte)0xea, (byte)0xfb, (byte)0xff, (byte)0x07,
		    (byte)0xc6, (byte)0x23, (byte)0x99, (byte)0x48, (byte)0xdb, (byte)0xc2, (byte)0xc4, (byte)0x03,
		    (byte)0xbf, (byte)0x98, (byte)0x65, (byte)0xf9, (byte)0x77, (byte)0xef, (byte)0x35, (byte)0x87		    
    });
	private static BigInteger g = new BigInteger(new byte[]{7});
	
	
	
	private String userName;
	private String authName;
	private SecureRandom rnd = new SecureRandom();	
	private BigInteger myExp;
	private byte[] K = new byte[16];
	private byte[] nonce = new byte[16];
	
	public PWServiceDHX(String userName){
		this.userName = userName;
		this.authName = userName;
		
		byte[] rndk= new byte[128];
		rnd.nextBytes(rndk);
		myExp = new BigInteger(rndk).abs();
		
	}
	
	/*
	 * returns username 0 authname 0 HDSD {128 bytes public key}
	 */
	public String clientStep1(){
		StringBuilder bldr = new StringBuilder();
		
		bldr.append(userName);
		bldr.append((char)0);
		bldr.append(authName);
		bldr.append((char)0);
		bldr.append(header);
		byte[] pubKey = UnsignedBIConverter.toByteArray(g.modPow(myExp, p));
		bldr.append(new String(pubKey));
		
		return bldr.toString();
	}
	
	/*
	 * response is {128 byte public key} CAST({16 bytes nonce}{16 bytes 0})
	 * must reply CAST({16 bytes nonce+1}{64 bytes password pad zeroes end})
	 */
	public String clientStep2(String response, String password) throws InvalidKeyException, IllegalStateException{
		
		byte[] respBytes = HexBuffer.hexToBin(response.toLowerCase().getBytes());
		
		byte[] srvpubk   = new byte[128];
		System.arraycopy(respBytes, 0, srvpubk, 0, srvpubk.length);
		BigInteger srvPubKey = UnsignedBIConverter.fromByteArray(srvpubk); 
		
		byte[] secret = UnsignedBIConverter.toByteArray(srvPubKey.modPow(myExp, p));
		System.arraycopy(secret, secret.length-K.length, K, 0, K.length);

		
		byte[] noncepad = castDecrypt(respBytes,srvpubk.length);
		System.arraycopy(noncepad,0,nonce,0,nonce.length);
		
		byte[] cnonce = UnsignedBIConverter.toByteArray(UnsignedBIConverter.fromByteArray(nonce).add(BigInteger.ONE));
		
		StringBuilder bldr = new StringBuilder();
		bldr.append(new String(cnonce));
		bldr.append(password);
		while(bldr.length()<80)
			bldr.append((char)0);
		
		return new String(castEncrypt(bldr.toString().getBytes(),0));
	}

	private byte[] castEncrypt(byte[] in,int offset) throws InvalidKeyException, IllegalStateException{

		IBlockCipher castCipher = CipherFactory.getInstance("cast5");
		IMode castCBCout = ModeFactory.getInstance("cbc", castCipher, 8);
			
		Map<String,Object> attributes = new HashMap<String,Object>();
		
		attributes.put(IMode.MODE_BLOCK_SIZE, new Integer(8));
		attributes.put(IMode.IV, clIV);
		attributes.put(IMode.STATE, new Integer(IMode.ENCRYPTION));
		attributes.put(IBlockCipher.CIPHER_BLOCK_SIZE, new Integer(8));
	    attributes.put(IBlockCipher.KEY_MATERIAL, K);
		    
	    castCBCout.init(attributes);

	    int nblocks = (in.length-offset)/castCBCout.currentBlockSize();

		byte[] buffer = new byte[castCBCout.currentBlockSize()*nblocks];

		for(int i=0;i<nblocks;++i)
			castCBCout.encryptBlock(in, offset+castCBCout.currentBlockSize()*i, buffer, castCBCout.currentBlockSize()*i);

		return buffer;
	}
	
	private byte[] castDecrypt(byte[] in,int offset) throws InvalidKeyException, IllegalStateException{

		IBlockCipher castCipher = CipherFactory.getInstance("cast5");
		IMode castCBCIn = ModeFactory.getInstance("cbc", castCipher, 8);
			
		Map<String,Object> attributes = new HashMap<String,Object>();
		
		attributes.put(IMode.MODE_BLOCK_SIZE, new Integer(8));
		attributes.put(IMode.IV, srIV);
		attributes.put(IMode.STATE, new Integer(IMode.DECRYPTION));
		attributes.put(IBlockCipher.CIPHER_BLOCK_SIZE, new Integer(8));
	    attributes.put(IBlockCipher.KEY_MATERIAL, K);
		    
		castCBCIn.init(attributes);
		
		int nblocks = (in.length-offset)/castCBCIn.currentBlockSize();

		byte[] buffer = new byte[castCBCIn.currentBlockSize()*nblocks];

		for(int i=0;i<nblocks;++i)
			castCBCIn.decryptBlock(in, offset+castCBCIn.currentBlockSize()*i, buffer, castCBCIn.currentBlockSize()*i);

		return buffer;
	}
	
	
}
