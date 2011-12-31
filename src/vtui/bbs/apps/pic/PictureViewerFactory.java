package vtui.bbs.apps.pic;

import textmode.curses.application.Application;
import textmode.curses.application.SingleInstanceApplicationFactory;

public class PictureViewerFactory extends SingleInstanceApplicationFactory {
	
	@Override
	public String getDisplayName() {
		return "Picture Viewer";
	}

	@Override
	public Application newInstance() {
		return new PictureViewer();
	}


}
