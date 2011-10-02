package vtui.bbs.ui;

import java.io.IOException;

import textmode.curses.CursesFactory;
import textmode.curses.application.Screen;
import textmode.curses.application.ScreenFactory;
import textmode.curses.term.Terminal;
import vtui.bbs.apps.login.DomainParameters;
import vtui.bbs.apps.login.Login;

public class BBSScreenFactory extends ScreenFactory {
	
	private DomainParameters domain;
	
	public BBSScreenFactory(){
		domain = DomainParameters.fromFile("settings/domain.xml");
	}
	
	@Override
	public Screen createScreen(Terminal t , CursesFactory cf){
		try {
			
			Screen myScr = new Screen(t,cf,new Login());
			myScr.session().put("BBSDomain", domain);
			return myScr;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
