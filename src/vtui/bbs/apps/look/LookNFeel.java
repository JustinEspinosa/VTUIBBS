package vtui.bbs.apps.look;

import textmode.curses.application.Application;

public class LookNFeel extends Application {

	private LookNFeelWindow window;
	private ThemeRepository repository = new ThemeRepository();
	
	@Override
	public void stop() {
	}

	@Override
	public void start() {
		window = new LookNFeelWindow(repository, this, curses(), nextPosition());
		showWindow(window);
	}

	@Override
	protected String name() {
		return "LookNFeel";
	}

}
