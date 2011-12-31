package vtui.bbs.session;

import textmode.data.Property;
import textmode.data.PropertyList;
import textmode.data.sql.SQLPersistenceParameters;
import textmode.data.xml.XMLPersistenceParameters;

public class SessionStorage extends PropertyList {
	
	@Property(persistant=true)
	public String storageType = "textmode.data.sql.SQLPersistor";

	@Property(persistant=true)
	public String paramAttr = "sqlp";
	
	@Property(persistant=true)
	public XMLPersistenceParameters xmlp = new XMLPersistenceParameters("");

	@Property(persistant=true)
	public SQLPersistenceParameters sqlp = new SQLPersistenceParameters("");

	
	
}
