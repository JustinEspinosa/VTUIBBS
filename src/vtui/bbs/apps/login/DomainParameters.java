package vtui.bbs.apps.login;

import textmode.data.PListPersistenceException;
import textmode.data.Property;
import textmode.data.PropertyList;
import textmode.data.xml.XMLPListPersistor;
import textmode.data.xml.XMLPersistenceParameters;

public class DomainParameters extends PropertyList{

	@Property(persistant=true)
	public String ldapHost = "";
	
	@Property(persistant=true)
	public String ldapBaseDN = "";
	
	public static DomainParameters fromFile(String pathName){
		XMLPersistenceParameters params = new XMLPersistenceParameters();
		params.pathName = pathName;
		XMLPListPersistor<DomainParameters> persistor = new XMLPListPersistor<DomainParameters>(params);
		try {
			return persistor.read(new DomainParameters());
		} catch (PListPersistenceException e) {
			return null;
		}
	}
	
}
