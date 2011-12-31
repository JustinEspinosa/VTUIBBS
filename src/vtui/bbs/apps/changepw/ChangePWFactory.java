package vtui.bbs.apps.changepw;

import textmode.curses.application.Application;
import textmode.curses.application.SingleInstanceApplicationFactory;

public class ChangePWFactory extends SingleInstanceApplicationFactory {

	@Override
	public Application newInstance() {
		return new ChangePassword();
	}

	@Override
	public String getDisplayName() {
		return "Change password";
	}

}
