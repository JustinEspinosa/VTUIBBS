package vtui.bbs.util.domain;

import gnu.crypto.Registry;
import gnu.crypto.key.IKeyAgreementParty;
import gnu.crypto.key.IKeyPairGenerator;
import gnu.crypto.key.KeyAgreementException;
import gnu.crypto.key.KeyPairGeneratorFactory;
import gnu.crypto.key.OutgoingMessage;
import gnu.crypto.key.dh.DiffieHellmanKeyAgreement;
import gnu.crypto.key.dh.DiffieHellmanSender;
import gnu.crypto.key.dh.RFC2631;
import gnu.crypto.util.PRNG;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.HashMap;

import textmode.xfer.util.ByteBuffer;

public class DHX {
	
	public static byte[] generateCNonce(){
		SecureRandom rnd = new SecureRandom();
		byte[] tab = new byte[16];
		rnd.nextBytes(tab);
		return tab;
	}
	private static byte C2SIV[] = { 0x4c, 0x57, 0x61, 0x6c, 0x6c, 0x61, 0x63, 0x65  };
	private static byte S2CIV[] = { 0x43, 0x4a, 0x61, 0x6c, 0x62, 0x65, 0x72, 0x74  };
	
	private byte[] cnonce;
	private byte[] castkey;
	
	
	private static int l = 1024;
	private static int m = 121;
	
	private BigInteger seed;
	private BigInteger counter;
	private BigInteger q;
	private BigInteger p;
	private BigInteger j;
	private BigInteger g;
	private BigInteger x;
	private BigInteger y;
	
	
	
	public DHX(){
		generate();
	}
	
	private byte[] readBuffer(ByteBuffer buf){
		byte[] ret = new byte[buf.remaining()];
		buf.get(ret);
		return ret;
	}
	
	private short sessionId(){
		int a = hashCode();
		return (short)(((a >> 8) ^ a) & 0xffff);
	}
	
	private void putPString(ByteBuffer buf, String str){
		buf.put((byte)str.length());
		buf.put(str.getBytes());
		if(str.length()%2!=0)
			buf.put((byte)' ');
	}
	
	public byte[] packUserNameWithMa(String username){
		ByteBuffer buf = ByteBuffer.allocate(512);
		
		IKeyPairGenerator kpg = KeyPairGeneratorFactory.getInstance(Registry.DH_KPG);
		kpg.setup(new HashMap<String,Object>()); // use default values
		KeyPair kpA = kpg.generate();
		IKeyAgreementParty A = new DiffieHellmanSender();
		     
		HashMap<String,Object> mapA = new HashMap<String,Object>();
		mapA.put(DiffieHellmanKeyAgreement.KA_DIFFIE_HELLMAN_OWNER_PRIVATE_KEY,kpA.getPrivate());

		//putPString(buf,username);
		try{   
			A.init(mapA);
		     
			OutgoingMessage out = A.processMessage(null);
			buf.put(out.wrap());
			
		}catch(KeyAgreementException e){
			e.printStackTrace();
			return null;
		}
		/*
		
		
		byte[] Ma = new byte[16];
		SecureRandom rnd = new SecureRandom();
		rnd.nextBytes(Ma);
		buf.put(Ma);
		/*
		buf.putShort(sessionId());
		
		buf.put(cnonce);
		
		byte[] Ma = y.toByteArray();
		System.out.println(Ma.length);
		byte[] pMa = new byte[m];
		System.arraycopy(Ma, 0, pMa, pMa.length-Ma.length, Ma.length);
		buf.put(pMa);*/
		
		buf.flip();
		return readBuffer(buf);
		
	}
	
	public byte[] continueLogin(byte[] challenge, String password){
		ByteBuffer buf = ByteBuffer.allocate(512);
		
		
		buf.flip();
		return readBuffer(buf);
	}

	
	private void generate(){
		BigInteger[] params = new RFC2631(m, l, null).generateParameters();
		seed = params[RFC2631.DH_PARAMS_SEED];
		counter = params[RFC2631.DH_PARAMS_COUNTER];
		q = params[RFC2631.DH_PARAMS_Q];
		p = params[RFC2631.DH_PARAMS_P];
		j = params[RFC2631.DH_PARAMS_J];
		g = params[RFC2631.DH_PARAMS_G];
		
		// generate a private number x of length m such as: 1 < x < q - 1
		BigInteger q_minus_1 = q.subtract(BigInteger.ONE);
		byte[] mag = new byte[(m + 7) / 8];
		while (true) {
			PRNG.nextBytes(mag);
			x = new BigInteger(1, mag);
			if(x.bitLength()==m  && x.compareTo(BigInteger.ONE)>0  && x.compareTo(q_minus_1)<0)
				break;
        }
		y = g.modPow(x, p);
		
		cnonce = generateCNonce();
	}
	
	public ByteBuffer setup() throws KeyAgreementException{
		ByteBuffer buf = ByteBuffer.allocate(512);
		
		buf.putShort(sessionId());
		buf.putInt((int)(0xffffffffL&g.longValue()));
		
		byte[] prime = p.toByteArray();
		buf.putShort((short)prime.length);
		buf.put(prime);
		
		byte[] Ma = new byte[prime.length];
		byte[] yarr = y.toByteArray();
		System.arraycopy(yarr, 0, Ma, Ma.length-yarr.length, yarr.length);
		buf.put(Ma);
		
		buf.flip();
		
		return buf;
	}
	

}
