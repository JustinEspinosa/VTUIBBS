package vtui.bbs.apps.userlist;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import vtui.bbs.apps.login.LdapData;

import fun.useless.curses.Curses;
import fun.useless.curses.application.Application;
import fun.useless.curses.ui.Dimension;
import fun.useless.curses.ui.Position;
import fun.useless.curses.ui.components.ListBox;
import fun.useless.curses.ui.components.Window;
import fun.useless.curses.ui.data.ListBoxModel;

public class UserListWindow extends Window {
	
	
	private class UserListModel implements ListBoxModel{

		private List<Map<String,Object> >  ldapData;
		
		public UserListModel(List<Map<String,Object> > data ){
			ldapData = data;
		}
		
		public int getItemCount() {
			return ldapData.size();
		}

		public Object getItemAt(int index) {
			Map<String,Object> user = ldapData.get(index);
			String displayItem ="";
			if( user.get("givenName") !=null )
				displayItem+=user.get("givenName")+ " ";
			displayItem+= user.get("sn") + " ("+user.get("uid")+")";
			
			return displayItem;
			
		}
		
	}
	
	private LdapData ldap = new LdapData();
	private ListBox list;

	public UserListWindow(Application app,Curses cs, Dimension dimension) {
		super("User list", app, cs,app.getWindowManager().getNextWindowPosition(), dimension);
		List<Map<String, Object>> users = ldap.getUserList();
		if(users==null)
			users = new Vector<Map<String,Object>>();
		
		UserListModel model = new UserListModel(users);
		
		list = new ListBox(model, curses(),new Position(1,1), dimension.mutate(-2,-2));
		intAddChild(list);
		setFocusNoNotify(list);
	}
	
	@Override
	protected void userResized() {
		list.setSize(getSize().mutate(-2,-2));
	}

}
