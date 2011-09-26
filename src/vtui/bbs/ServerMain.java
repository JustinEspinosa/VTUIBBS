package vtui.bbs;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import vtui.bbs.ui.BBSScreenFactory;
import vtui.bbs.ui.UITheme;
import fun.useless.curses.DefaultCursesFactory;
import fun.useless.curses.net.TLSGeneralSocketIO;
import fun.useless.curses.net.TelnetServer;
import fun.useless.curses.term.io.TelnetIO;
import fun.useless.curses.ui.util.SortedList;

@SuppressWarnings("unused")
public class ServerMain {

	private static class LoggingHandler extends Handler{

		private Map<String,List<Level>> blist = new HashMap<String, List<Level>>();
		private Comparator<Level> lvlComp = new Comparator<Level>() {

			public int compare(Level o1, Level o2) {
				if(o1.intValue() < o2.intValue())
					return -1;	
				
				if(o1.intValue() > o2.intValue())
					return 1;
				
				return 0;
			}
		};
		
		
		private List<Level> getBLLevels(String name){
			if(!blist.containsKey(name))
				blist.put(name, new SortedList<Level>(lvlComp) );

			return blist.get(name);
		}
		
		public void blacklist(Class<?> source, Level level){
			
			List<Level> list = getBLLevels(source.getCanonicalName());
			list.add(level);
			
		}
		
		private boolean isBlackListed(String name, Level level){
			return getBLLevels(name).contains(level);
		}
		
		@Override
		public void publish(LogRecord record) {
			if(!isBlackListed(record.getSourceClassName(),record.getLevel())){
				System.out.println(record.getMessage());
			}
		}

		@Override
		public void flush() { }

		@Override
		public void close() throws SecurityException { }
		
	}
	
	private static SSLContext prepareSSLContext() throws GeneralSecurityException, IOException{
			
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream("keys.store"), "vtuibbs".toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, "vtuibbs".toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		
		
		SSLContext ctx = SSLContext.getInstance("SSL");
		
		ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

		return ctx;
	}
	
	/**
	 * @param args
	 * @throws  
	 */
	public static void main(String[] args)  {
		int port = 9003;
		LoggingHandler handler = new LoggingHandler();
		Logger logger = Logger.getAnonymousLogger();
		logger.setLevel(Level.SEVERE);
		
		
		/* 
		 * 
		 * handler.blacklist(TelnetIO.class, Level.FINE);
		 * handler.blacklist(TLSGeneralSocketIO.class, Level.FINE);
		 * handler.blacklist(TLSGeneralSocketIO.class, Level.FINER);
		 * handler.blacklist(TelnetServer.class,Level.INFO);
		 * handler.blacklist(ServerMain.class,Level.INFO);
		 * 
		 * logger.addHandler(handler);
		 * 
		 */

		
		try {
			
			SSLContext ctx = prepareSSLContext();
			
			logger.severe("Visual Text-based User Interface BBS Server.");
			
			if(args.length>0){
				try{
					port = Integer.parseInt(args[0]);
				}catch(NumberFormatException e){ e.printStackTrace(); }
			}
			
			UITheme.applyColors();
			
			TelnetServer server = new TelnetServer(null, new DefaultCursesFactory( "termcap.src") , new BBSScreenFactory());
			
			logger.severe("Listening on IP:0.0.0.0, TCP:"+port+" (TLS).");
			
			server.acceptConnections(port,ctx);
			
		} catch (IOException e) {
			
			logger.severe("FATAL CATASTROPHIC ERROR! CALAMITY!");
			e.printStackTrace();
			System.exit(-1);
			
		} catch (NoSuchAlgorithmException e){
			
			logger.severe("Irgend ein Algorithmus in ein puffer.");
			e.printStackTrace();
			System.exit(-1);
			
		} catch (GeneralSecurityException e) {
			
			logger.severe("Sorry, not PCI compliant.");
			e.printStackTrace();
			System.exit(-1);
			
		}


	}

}
