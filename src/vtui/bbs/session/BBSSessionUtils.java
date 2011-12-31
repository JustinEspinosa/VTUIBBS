package vtui.bbs.session;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import textmode.data.PListPersistenceException;
import textmode.data.PropertyList;
import textmode.data.PropertyListPersistor;
import textmode.data.xml.XMLPListPersistor;
import textmode.data.xml.XMLPersistenceParameters;


@SuppressWarnings("unchecked")
public final class BBSSessionUtils {
	
	private static PropertyListPersistor<BBSSession> persistor;
	
	
	static{
		
		XMLPListPersistor<SessionStorage> confLoader = new XMLPListPersistor<SessionStorage>(new XMLPersistenceParameters("settings/persistence.xml"));  
		try {
			SessionStorage config = confLoader.read(new SessionStorage());
			Class<?> persClass = Class.forName(config.storageType);
			Constructor<?> constr = persClass.getConstructor(PropertyList.class);
			persistor = (PropertyListPersistor<BBSSession>) constr.newInstance(config.get(config.paramAttr, config.typeOf(config.paramAttr)));
			
		} catch (PListPersistenceException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	
	public static BBSSession loadSession(String userId){
		BBSSession keyS = new BBSSession();
		keyS.set("userId", userId);
		try {
			return persistor.read(keyS);
		} catch (PListPersistenceException e) {
			e.printStackTrace();
		}
		return keyS;
	}
	
	public static void saveSession(BBSSession sess){

		try {
			persistor.write(sess);
		} catch (PListPersistenceException e) {
			e.printStackTrace();
		}
		
	}
}
