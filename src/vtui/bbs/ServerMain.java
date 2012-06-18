package vtui.bbs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
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

import textmode.curses.DefaultCursesFactory;
import textmode.curses.net.TLSGeneralSocketIO;
import textmode.curses.net.TelnetServer;
import textmode.curses.term.io.TelnetIO;
import textmode.curses.ui.util.SortedList;
import textmode.data.PListPersistenceException;
import textmode.data.xml.XMLPListPersistor;
import textmode.data.xml.XMLPersistenceParameters;
import vtui.bbs.ui.BBSScreenFactory;
import vtui.bbs.ui.UITheme;

@SuppressWarnings("unused")
public class ServerMain {
	
	private static String generateName(String prefix,Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		StringBuilder builder = new StringBuilder();
		builder.append(prefix);
		builder.append('.');
		builder.append(cal.get(Calendar.YEAR));
		builder.append('-');
		builder.append(String.format("%02d",cal.get(Calendar.MONTH)));
		builder.append('-');
		builder.append(String.format("%02d",cal.get(Calendar.DAY_OF_MONTH)));
		builder.append('_');
		builder.append(String.format("%02d",cal.get(Calendar.HOUR_OF_DAY)));
		builder.append('.');
		builder.append(String.format("%02d",cal.get(Calendar.MINUTE)));
		builder.append('.');
		builder.append(String.format("%02d",cal.get(Calendar.SECOND)));
		return builder.toString();
	}

	private static class FilesLoggingHandler extends Handler{
		private static String FileName = "logs/vtuibbs.log";
		private PrintWriter out;
		
		private FilesLoggingHandler(){
			try {
				File f = new File(FileName);
				if(f.exists()){
					Date fdate =  new Date(f.lastModified());
					f.renameTo(new File(generateName(FileName,fdate)));
					f = new File(FileName);
				}
				
				out = new PrintWriter(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void publish(LogRecord record) {
			out.printf("%s - %s\n",(new Date()),record.getThreadID());
			out.println(record.getMessage());
			Throwable excp = record.getThrown();
			if(excp!=null)
				excp.printStackTrace(out);
			out.flush();
		}

		@Override
		public void flush() {
			out.flush();
		}

		@Override
		public void close() throws SecurityException {
			out.close();
		}
		
	}
	
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
		
		//LoggingHandler handler = new LoggingHandler();
		Logger logger = Logger.getAnonymousLogger();
		
		logger.setLevel(Level.ALL);
		logger.setUseParentHandlers(false);
		logger.addHandler(new FilesLoggingHandler());

		XMLPListPersistor<Configuration> ppersist = XMLPListPersistor.fromParameterFile("settings/config.xml", Configuration.class);
		Configuration config = new Configuration();
		
		//load FS handlers
		try {
			Class.forName("vtui.bbs.util.FileSystem");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			config = ppersist.read(config);
			logger.setLevel(Level.parse(config.logLevel));
			int port = config.port;
		
			
			SSLContext ctx = prepareSSLContext();
			
			logger.config("Visual Text-based User Interface BBS Server.");
			
			if(args.length>0){
				try{
					port = Integer.parseInt(args[0]);
				}catch(NumberFormatException e){ e.printStackTrace(); }
			}
			
			UITheme.applyColors();
			
			TelnetServer server = new TelnetServer(logger, new DefaultCursesFactory( "termcap.src") , new BBSScreenFactory());
			
			logger.config("Listening on IP:0.0.0.0, TCP:"+port+" (TLS).");
			
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
			
		} catch (PListPersistenceException e) {
			logger.log(Level.SEVERE,"Problem reading configuration",e);
			e.printStackTrace();
			System.exit(-1);
		}


	}

}
