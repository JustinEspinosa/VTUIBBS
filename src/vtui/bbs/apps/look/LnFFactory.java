package vtui.bbs.apps.look;

import textmode.curses.application.Application;
import textmode.curses.application.SingleInstanceApplicationFactory;

public class LnFFactory extends SingleInstanceApplicationFactory {

	@Override
	public Application newInstance() {
		return new LookNFeel();
	}

	@Override
	public String getDisplayName() {
		return "Customize";
	}

}
