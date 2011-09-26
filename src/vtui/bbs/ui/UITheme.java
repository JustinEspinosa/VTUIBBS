package vtui.bbs.ui;

import fun.useless.curses.ui.AnsiColor16;
import fun.useless.curses.ui.AnsiColor8;
import fun.useless.curses.ui.ColorDefaults;
import fun.useless.curses.ui.ColorPair;
import fun.useless.curses.ui.ColorType;
import fun.useless.curses.ui.ColorDefaults.ColorDepth;
import fun.useless.curses.ui.XTermColor256;

public class UITheme {
	public static void applyColors(){
		
		ColorDefaults.setDefaultColor(ColorType.MENU,new ColorPair(AnsiColor8.Green,AnsiColor8.Black),ColorDepth.COL8);
		ColorDefaults.setDefaultColor(ColorType.WINDOW,new ColorPair(AnsiColor8.Green,AnsiColor8.Black),ColorDepth.COL8);
		ColorDefaults.setDefaultColor(ColorType.SELECTED,new ColorPair(AnsiColor8.White,AnsiColor8.Blue),ColorDepth.COL8);
		ColorDefaults.setDefaultColor(ColorType.TITLEBAR,new ColorPair(AnsiColor8.Yellow,AnsiColor8.Red),ColorDepth.COL8);
		ColorDefaults.setDefaultColor(ColorType.GREYED,new ColorPair(AnsiColor8.Black,AnsiColor8.White),ColorDepth.COL8);
		ColorDefaults.setDefaultColor(ColorType.BUTTON,new ColorPair(AnsiColor8.Black,AnsiColor8.Cyan),ColorDepth.COL8);
		ColorDefaults.setDefaultColor(ColorType.EDIT,new ColorPair(AnsiColor8.Black,AnsiColor8.White),ColorDepth.COL8);
		ColorDefaults.setDefaultColor(ColorType.DESKTOP,new ColorPair(AnsiColor8.Green,AnsiColor8.Green),ColorDepth.COL8);


		ColorDefaults.setDefaultColor(ColorType.MENU,new ColorPair(AnsiColor16.LightGreen,AnsiColor16.Black),ColorDepth.COL16);
		ColorDefaults.setDefaultColor(ColorType.WINDOW,new ColorPair(AnsiColor16.Green,AnsiColor16.Black),ColorDepth.COL16);
		ColorDefaults.setDefaultColor(ColorType.SELECTED,new ColorPair(AnsiColor16.White,AnsiColor16.LightBlue),ColorDepth.COL16);
		ColorDefaults.setDefaultColor(ColorType.TITLEBAR,new ColorPair(AnsiColor16.Yellow,AnsiColor16.LightRed),ColorDepth.COL16);
		ColorDefaults.setDefaultColor(ColorType.GREYED,new ColorPair(AnsiColor16.Gray,AnsiColor16.DarkGray),ColorDepth.COL16);
		ColorDefaults.setDefaultColor(ColorType.BUTTON,new ColorPair(AnsiColor16.Black,AnsiColor16.Gray),ColorDepth.COL16);
		ColorDefaults.setDefaultColor(ColorType.EDIT,new ColorPair(AnsiColor16.Black,AnsiColor16.White),ColorDepth.COL16);
		ColorDefaults.setDefaultColor(ColorType.DESKTOP,new ColorPair(AnsiColor16.LightGreen,AnsiColor16.LightGreen),ColorDepth.COL16);
		
		
		ColorDefaults.setDefaultColor(ColorType.MENU,new ColorPair(new XTermColor256(0,0,0),new XTermColor256(200,200,200)),ColorDepth.COL256);
		ColorDefaults.setDefaultColor(ColorType.WINDOW,new ColorPair(new XTermColor256(0,0,0),new XTermColor256(150,150,150)),ColorDepth.COL256);
		ColorDefaults.setDefaultColor(ColorType.SELECTED,new ColorPair(new XTermColor256(250,250,250),new XTermColor256(0,0,240)),ColorDepth.COL256);
		ColorDefaults.setDefaultColor(ColorType.TITLEBAR,new ColorPair(new XTermColor256(200,200,0),new XTermColor256(180,0,0)),ColorDepth.COL256);
		ColorDefaults.setDefaultColor(ColorType.GREYED,new ColorPair(new XTermColor256(50,50,50),new XTermColor256(150,150,150)),ColorDepth.COL256);
		ColorDefaults.setDefaultColor(ColorType.BUTTON,new ColorPair(new XTermColor256(0,0,0),new XTermColor256(180,180,180)),ColorDepth.COL256);
		ColorDefaults.setDefaultColor(ColorType.EDIT,new ColorPair(new XTermColor256(0,0,0),new XTermColor256(250,250,250)),ColorDepth.COL256);
		ColorDefaults.setDefaultColor(ColorType.DESKTOP,new ColorPair(new XTermColor256(60,200,200),new XTermColor256(60,200,200)),ColorDepth.COL256);
		
	}

}
