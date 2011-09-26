package vtui.bbs.apps.userlist;

import fun.useless.curses.application.Application;
import fun.useless.curses.ui.Dimension;

public class UserList extends Application {

	private UserListWindow myWindow;
	
	@Override
	public void stop() {

	}

	@Override
	public void start() {
		myWindow = new UserListWindow(this, curses(),new Dimension(10, 20));
		showWindow(myWindow);
	}

	@Override
	protected String name() {
		return "User list";
	}

}
