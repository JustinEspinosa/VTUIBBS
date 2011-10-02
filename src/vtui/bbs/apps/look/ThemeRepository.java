package vtui.bbs.apps.look;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import textmode.curses.application.Screen;
import textmode.curses.ui.WindowManager;
import textmode.curses.ui.look.ColorTheme;
import textmode.curses.ui.look.InvalidFileTypeException;
import textmode.curses.ui.look.ThemeFileParser;
import vtui.bbs.session.BBSSession;


public class ThemeRepository extends HashMap<String,ColorTheme>{

	private static final long serialVersionUID = -7908944832009294107L;
	private ThemeFileParser parser;
	
	public ThemeRepository(){
		try{
			parser = new ThemeFileParser();
			loadThemes("themes");
		}catch(ParserConfigurationException e){
			e.printStackTrace();
		}
	}
	
	private void loadThemes(String path){
		File dir = new File(path);
		
		File[] files = dir.listFiles(new FilenameFilter() {			
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});
				
		for(File file:files){
			
			try {
				ColorTheme[] themes = parser.parse(file);
				
				for(ColorTheme theme:themes){
					put(theme.name(),theme);
				}
				
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidFileTypeException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void useTheme(WindowManager wMan,String themeName){
		ColorTheme theme = get(themeName);
		wMan.setColorTheme(theme);
		BBSSession sess = Screen.currentSession().getAsChecked("BBSSession",BBSSession.class);
		sess.uiTheme = themeName;
	}
	
}
