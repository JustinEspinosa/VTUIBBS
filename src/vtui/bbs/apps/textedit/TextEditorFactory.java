package vtui.bbs.apps.textedit;

import textmode.curses.application.Application;
import textmode.curses.application.SingleInstanceApplicationFactory;

public class TextEditorFactory extends SingleInstanceApplicationFactory {
	
	@Override
	public String getDisplayName() {
		return "Text Editor";
	}

	@Override
	public Application newInstance() {
		return new TextEditor();
	}


}
