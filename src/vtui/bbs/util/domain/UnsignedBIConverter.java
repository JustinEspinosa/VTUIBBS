package vtui.bbs.util.domain;

import java.math.BigInteger;

public final class UnsignedBIConverter{
	public static BigInteger fromByteArray(byte[] barr){
		if( (barr[0] & 0x80) == 0x80 ){
			byte[] ubarr = new byte[barr.length+1];
			ubarr[0] = 0;
			System.arraycopy(barr, 0, ubarr, 1, barr.length);
			return new BigInteger(ubarr);
		}
		return new BigInteger(barr);
	}
	public static byte[] toByteArray(BigInteger bi){
		byte[] barr = bi.toByteArray();
		if( barr[0] == 0){
			byte[] ubarr = new byte[barr.length-1];
			System.arraycopy(barr,1,ubarr,0,ubarr.length);
			return ubarr;
		}
		return barr;
	}
}