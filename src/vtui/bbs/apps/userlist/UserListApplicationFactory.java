package vtui.bbs.apps.userlist;

import textmode.curses.application.Application;
import textmode.curses.application.SingleInstanceApplicationFactory;

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
