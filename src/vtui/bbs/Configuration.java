package vtui.bbs;

import textmode.data.Property;
import textmode.data.PropertyList;

public class Configuration extends PropertyList{
	@Property(persistant=true)
	public String logLevel = "INFO"; 
	
	@Property(persistant=true)
	public Integer port = 9000; 
	
	@Property(persistant=true)
	public Boolean ssl = true; 
}
