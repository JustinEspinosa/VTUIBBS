package vtui.bbs.session;

import textmode.data.PListPersistenceException;
import textmode.data.xml.XMLPListPersistor;

public final class BBSSessionUtils {
	
	private static XMLPListPersistor<BBSSession> persistor = XMLPListPersistor.fromParameterFile("settings/bbs/sessions.xml", BBSSession.class);
	
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
