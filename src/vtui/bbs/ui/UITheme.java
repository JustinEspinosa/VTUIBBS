package vtui.bbs.ui;

import textmode.curses.ui.ColorDefaults;
import textmode.curses.ui.ColorPair;
import textmode.curses.ui.ColorType;
import textmode.curses.ui.XTermColor256;

public class UITheme {
	
	public static void applyColors(){
		
		ColorDefaults.setDefaultColor(ColorType.MENU    ,new ColorPair(new XTermColor256(0,0,0),new XTermColor256(200,200,200))    );
		ColorDefaults.setDefaultColor(ColorType.WINDOW  ,new ColorPair(new XTermColor256(0,0,0),new XTermColor256(150,150,150))    );
		ColorDefaults.setDefaultColor(ColorType.SELECTED,new ColorPair(new XTermColor256(250,250,250),new XTermColor256(0,0,240))  );
		ColorDefaults.setDefaultColor(ColorType.TITLEBAR,new ColorPair(new XTermColor256(200,200,0),new XTermColor256(180,0,0))    );
		ColorDefaults.setDefaultColor(ColorType.GREYED  ,new ColorPair(new XTermColor256(50,50,50),new XTermColor256(150,150,150)) );
		ColorDefaults.setDefaultColor(ColorType.BUTTON  ,new ColorPair(new XTermColor256(0,0,0),new XTermColor256(180,180,180))    );
		ColorDefaults.setDefaultColor(ColorType.EDIT    ,new ColorPair(new XTermColor256(0,0,0),new XTermColor256(250,250,250))    );
		ColorDefaults.setDefaultColor(ColorType.DESKTOP ,new ColorPair(new XTermColor256(0,0,60),new XTermColor256(0,0,60)));
		
	}

}
