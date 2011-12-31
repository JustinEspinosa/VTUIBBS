package vtui.bbs.apps.userlist;

import textmode.curses.application.Application;
import textmode.curses.application.Screen;
import textmode.curses.ui.Dimension;
import vtui.bbs.apps.login.DomainParameters;
import vtui.bbs.util.domain.LdapData;

public class UserList extends Application {

	private UserListWindow myWindow;
	private LdapData ldap;
	
	@Override
	public void stop() {

	}

	@Override
	public void start() {
		DomainParameters domain = Screen.currentSession().getAsChecked("BBSDomain", DomainParameters.class);
		ldap = new LdapData(domain.ldapHost, domain.ldapBaseDN);
		myWindow = new UserListWindow(ldap,this, curses(),new Dimension(10, 20));
		showWindow(myWindow);
	}

	@Override
	protected String name() {
		return "User list";
	}

}
