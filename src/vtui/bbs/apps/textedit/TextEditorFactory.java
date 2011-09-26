package vtui.bbs.apps.textedit;

import fun.useless.curses.application.Application;
import fun.useless.curses.application.SingleInstanceApplicationFactory;

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
