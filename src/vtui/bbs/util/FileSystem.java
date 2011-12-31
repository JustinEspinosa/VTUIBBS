package vtui.bbs.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

public class FileSystem {
	
	private static Map<String,Class<? extends FileSystemAdapter> > adapters = new TreeMap<String, Class<? extends FileSystemAdapter> >();
	
	static{
		
		try {
			Class.forName("vtui.bbs.util.JCifsFileSystemAdapter");
			Class.forName("vtui.bbs.util.LocalFileSystemAdapter");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * Returns a new FileSystemAdapter suitable for URL in url
	 * @param url
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static FileSystemAdapter getAdapter(URL url) throws InstantiationException, IllegalAccessException{
		Class<? extends FileSystemAdapter> type = adapters.get(url.getProtocol());
		if(type==null)
			throw new InstantiationException("Unknown protocol in "+url);
		
		try {
			
			Constructor<? extends FileSystemAdapter> constructor = type.getConstructor(URL.class);
			return constructor.newInstance(url);
			
		} catch (SecurityException e) {
			throw new InstantiationException("No compatible constructor for "+type+": SecurityException: "+e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new InstantiationException("No compatible constructor for "+type+": NoSuchMethodException: "+e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new InstantiationException("No compatible constructor for "+type+": IllegalArgumentException: "+e.getMessage());
		} catch (InvocationTargetException e) {
			throw new InstantiationException("No compatible constructor for "+type+": InvocationTargetException: "+e.getMessage());
		}
		
	}
	
	public static void registerAdapter(String protocol, Class<? extends FileSystemAdapter> type){
		adapters.put(protocol, type);
	}
}
