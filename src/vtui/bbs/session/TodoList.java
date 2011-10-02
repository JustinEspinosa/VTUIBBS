package vtui.bbs.session;

import textmode.data.Property;
import textmode.data.PropertyList;

public class TodoList extends PropertyList {
	@Property(persistant=true)
	public String name = "Hello";
}
