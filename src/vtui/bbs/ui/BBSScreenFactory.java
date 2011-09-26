package vtui.bbs.ui;

import java.io.IOException;

import vtui.bbs.apps.login.Login;

import fun.useless.curses.CursesFactory;
import fun.useless.curses.application.Screen;
import fun.useless.curses.application.ScreenFactory;
import fun.useless.curses.term.Terminal;

public class BBSScreenFactory extends ScreenFactory {
	
	@Override
	public Screen createScreen(Terminal t , CursesFactory cf){
		try {
			
			Screen myScr = new Screen(t,cf,new Login());
			return myScr;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
