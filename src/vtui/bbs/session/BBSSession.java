package vtui.bbs.session;

import textmode.data.Property;
import textmode.data.PropertyList;


public class BBSSession extends PropertyList{
	
	@Property(persistant=true,key=true)
	public String userId = "";
	
	@Property(persistant=true)
	public String uiTheme = "";

}
