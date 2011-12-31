package vtui.bbs.util.domain;

import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import textmode.xfer.util.HexBuffer;

public class DigestMd5 {
	
	private static class NeutralString extends Vector<Byte> implements CharSequence{
		private static final long serialVersionUID = 8529099748732999376L;
		public NeutralString(){ }
		public NeutralString(String str){ this(str.getBytes()); }
		public NeutralString(CharSequence str){ for(int i=0;i<str.length();++i) add(new Byte((byte)str.charAt(i))); }
		public NeutralString(byte[] barr){ for(byte b:barr) add(b); }
		public int length() { return size(); }
		public char charAt(int index) { return (char)get(index).byteValue(); 
		}
		public CharSequence subSequence(int start, int end) {
			NeutralString sub = new NeutralString();
			for(int i=start;i<end;++i)
				try{
					sub.add(get(i));
				}catch(ArrayIndexOutOfBoundsException e){
					throw new StringIndexOutOfBoundsException(i);
				}
			return sub;
		}
		
		@Override
		public synchronized String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append(this);
			return buffer.toString();
		}
		public byte[] getBytes() {
			byte[] arr=new byte[size()];
			int index=0;
			for(Byte b:this) arr[index++]=b.byteValue();
			return arr;
		}
	}
	
	private static NeutralString n(String s){
		return new NeutralString(s);
	}
	
	private static NeutralString Colon = n(":");
	
	private Map<String,String> inAttributes = new HashMap<String,String>();
	private Map<String,String> outAttributes = new HashMap<String,String>();
	private int nonceCount = 0;
	private static MessageDigest md5;
	private static HashSet<String> qops = new HashSet<String>();
	
	static{
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		qops.add("auth");
	}
	
	public DigestMd5(String data){

		System.out.println(data);
		(new Parser()).parseInput(data);
	}
	
	private static enum ParserState{KEY,VALUE,QUOTEDVALUE}
	private class Parser{
		private String lastKey = null;
		private void resetBuilder(StringBuilder builder){
			builder.delete(0, builder.capacity()+1);
		}
		private ParserState gotComma(ParserState state,StringBuilder buffer){
			switch(state){
			case VALUE:
				inAttributes.put(lastKey,buffer.toString());
				//System.out.println(lastKey+"=>"+buffer.toString());
				resetBuilder(buffer);
				return ParserState.KEY;
			default:
				buffer.append(',');
			}
			return state;
		}
		private ParserState gotEquals(ParserState state,StringBuilder buffer){
			switch(state){
			case KEY:
				lastKey = buffer.toString();
				resetBuilder(buffer);
				return ParserState.VALUE;
			default:
				buffer.append('=');
			}
			return state;
		}
		private ParserState gotQuote(ParserState state){
			switch(state){
			case VALUE:
				return ParserState.QUOTEDVALUE;
			case QUOTEDVALUE:
				return ParserState.VALUE;
			}
			return state;
		}
		private void parseInput(String data){
			StringReader reader = new StringReader(data);
			int b;
			try{
			
				StringBuilder buffer = new StringBuilder();
				ParserState state = ParserState.KEY;
				
				while( (b=reader.read())!=-1){
					switch((char)b){
					case '"':
						state = gotQuote(state);
						break;
					case ',':
						state = gotComma(state,buffer);
						break;
					case '=':
						state = gotEquals(state,buffer);
						break;
					default:
						buffer.append((char)b);
					}
				
				}
				gotComma(state,buffer);
			
			}catch(IOException e){}
		}
	}

	private static List<String> order = new ArrayList<String>();
	private static HashSet<String> noQuote = new HashSet<String>();
	static{ 
		noQuote.add("charset");
		noQuote.add("response"); 
		noQuote.add("nc"); 
		noQuote.add("qop"); 
		

		order.add("nonce");
		order.add("realm");
		order.add("qop");
		order.add("nc");
		order.add("cnonce");
		order.add("username");
		order.add("digest-uri");
		order.add("response");
		order.add("maxbuf");
		order.add("charset");
		order.add("cipher");
		order.add("authzid");
		order.add("auth-param");
	}
	private static class FieldSorter implements Iterator<String>, Iterable<String>{
		private Set<String> keys;
		private Iterator<String> baseIterator = order.iterator();
		private String current = null;
		
		public FieldSorter(Set<String> k){
			keys = k;
			prepareNext();
		}
		private void prepareNext(){
			current = null;
			while(current==null && baseIterator.hasNext()){
				current = baseIterator.next();
				if(!keys.contains(current))
					current = null;
			}

		}
		public boolean hasNext() {
			return current!=null;
		}
		public String next() {
			String resp = current;
			prepareNext();
			return resp;
		}
		public void remove() {}
		public Iterator<String> iterator() {
			return this;
		}
	}
	private class Generator{
		
		private void addAttribute(StringBuilder buffer,String attribute, boolean noquote){
			if(noquote){
				buffer.append(attribute);
			}else{
				buffer.append('"');
				buffer.append(attribute);
				buffer.append('"');
			}
		}
		private void addEntry(StringBuilder buffer,String key,String value){
			buffer.append(key);
			buffer.append('=');
			addAttribute(buffer,value,noQuote.contains(key));
		}
		private String generate(){
			StringBuilder buffer = new StringBuilder();
			FieldSorter sorter = new FieldSorter(outAttributes.keySet());
			for(String key : sorter){
				if(buffer.length()>0)
					buffer.append(',');
				addEntry(buffer,key,outAttributes.get(key));
			}
				
			return buffer.toString();
		}
	}
	
	public String generate(){
		String gen = (new Generator()).generate();
		System.out.println(gen);
		return gen;
	}
	
	private NeutralString utf8ToIsoTrickForLegacyHttp(NeutralString value){
		StringBuilder buffer = new StringBuilder();
		StringReader  reader = new StringReader(value.toString());
		
		try{
		
		int c;
		while( (c=reader.read())!=-1){
			if((c&0xff)>0xc3) //non-iso-subset character;
				return new NeutralString(value);
			if((c&0xff)>0xbf)
				buffer.append((char)(  ((c&0x3) << 6) | (c&0x3f)  ));
			else
				buffer.append((char)c);
		}
		
		
		}catch(IOException ioe){}
		
		return new NeutralString(buffer);
	}
	
	public void putValue(String name,String value){
		outAttributes.put(name,value);
	}
	
	public String getOValue(String name){
		return outAttributes.get(name);
	}
	
	public String getValue(String name){
		return inAttributes.get(name);
	}
	
	public static String generateCNonce(){
		SecureRandom rnd = new SecureRandom();
		byte[] tab = new byte[16];
		for(int i=0;i<tab.length;++i){
			int letter = rnd.nextInt(26)+0x41;
			if(rnd.nextBoolean()) letter+=0x20;
			if(rnd.nextBoolean()) letter=0x30+rnd.nextInt(10);
			tab[i] = (byte)letter;
		}
		return new String(tab);
	}
	
	private String getMatching(HashSet<String> values,String prop){
		String[] servQops = getValue(prop).split(",");
		for(String qop : servQops){
			if(values.contains(qop))
				return qop;
		}
		return null;
	}
	
	private String getQop(){
		return getMatching(qops,"qop");
	}
	
	public static String cat(String ... str){
		StringBuilder buffer = new StringBuilder();
		for(String s: str)
			buffer.append(s);
		return new String(buffer);
	}
	private NeutralString cat(NeutralString ... str){
		StringBuilder buffer = new StringBuilder();
		for(NeutralString s: str)
			buffer.append(s);
		return new NeutralString(buffer);
	}
	
	public static String MD5(String value){
		return new String(HexBuffer.binToHex(md5.digest(value.getBytes())));
	}
	public static String H(String value){
		return new String(md5.digest(value.getBytes()));
	}
	public static String HEX(String input){
		return new String(HexBuffer.binToHex(input.getBytes()));
	}
	
	private NeutralString H(NeutralString value){
		if(getValue("charset").toLowerCase().equals("utf-8"))
			value = utf8ToIsoTrickForLegacyHttp(value);
		return new NeutralString(md5.digest(value.getBytes()));
	}
	
	private NeutralString KD(NeutralString v1,NeutralString v2){
		return H(cat(v1,Colon,v2));
	}
	
	private NeutralString HEX(NeutralString input){
		return new NeutralString(HexBuffer.binToHex(input.getBytes()));
	}
	
	private NeutralString A1(String password){
		NeutralString A1= cat(H(cat(n(getOValue("username")), Colon, n(getOValue("realm")),Colon, n(password) )),
		           Colon, n(getOValue("nonce")), Colon , n(getOValue("cnonce")));
		return A1;
	}
	private NeutralString A2(){
		NeutralString A2= cat(n("AUTHENTICATE:"),n(getOValue("digest-uri")));
		return A2;
	}
	private String getDigestUri(String srv,String host) {
		return cat(srv,"/",host);
	}

	public void prepare(String username,String password, String host, String service, String cnonce){

		putValue("maxbuf","4096");
		putValue("username",username);
		putValue("realm",getValue("realm"));
		putValue("nonce",getValue("nonce"));
		putValue("cnonce",cnonce);
		putValue("nc",String.format("%08x",++nonceCount));
		putValue("qop",getQop());
		putValue("charset",getValue("charset"));
		putValue("digest-uri",getDigestUri(service,host));
		NeutralString response = 
		HEX(KD ( HEX(H(A1(password))),
			cat( n(getOValue("nonce")), Colon, n(getOValue("nc")),Colon,
			  	 n(getOValue("cnonce")), Colon, n(getOValue("qop")),Colon, HEX(H(A2())))));
		putValue("response",response.toString());
	}


}
