package vtui.bbs.apps.userlist;

import fun.useless.curses.application.Application;
import fun.useless.curses.application.SingleInstanceApplicationFactory;

public class UserListApplicationFactory extends SingleInstanceApplicationFactory {

	@Override
	public String getDisplayName() {
		return "User list";
	}

	@Override
	public Application newInstance() {
		return new UserList();
	}

}
