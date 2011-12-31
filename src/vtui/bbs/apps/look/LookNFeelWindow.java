package vtui.bbs.apps.look;

import java.util.Enumeration;

import textmode.curses.Curses;
import textmode.curses.application.Application;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.Button;
import textmode.curses.ui.components.Component;
import textmode.curses.ui.components.Container;
import textmode.curses.ui.components.Label;
import textmode.curses.ui.components.ListBox;
import textmode.curses.ui.components.Window;
import textmode.curses.ui.data.ListModel;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;
import textmode.curses.ui.look.ColorTheme;


public class LookNFeelWindow extends Window {
	
	private class ThemeListModel implements ListModel{
		public int getItemCount() {
			return repository.size();
		}
		public Object getItemAt(int index) {
			return repository.keySet().toArray()[index];
		}
	}
	
	private ListBox themeList;
	private ThemePreview preview;
	private ThemeRepository repository;
	
	public LookNFeelWindow(ThemeRepository repo,Application app,Curses curses,Position p){
		super("Look n' Feel",app,curses,p,new Dimension(14,35));
		repository = repo;
		setResizeable(false);
		addComponents();
	}
	
	@SuppressWarnings("unchecked")
	private void setThemeRecurse(Component c, ColorTheme theme){
		if(c instanceof Container<?>){
			Container<Component> comp = (Container<Component>)c;
			Enumeration<Component> children = comp.children();
			while(children.hasMoreElements())
				setThemeRecurse(children.nextElement(),theme);
		}
		
		c.setColorManager(theme.getColorManager());
	}

	private synchronized void applyTheme(String themeName){
		ColorTheme theme = repository.get(themeName);
		
		setThemeRecurse(this,theme);
		
		refresh();
	}
	
	private void addComponents(){
		intAddChild(new Label("Themes",curses(),new Position(1,0), new Dimension(1, 10)));
		themeList = new ListBox(new ThemeListModel(), curses(), new Position(2,0), new Dimension(9,10));
		themeList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyTheme(themeList.selectedItem().toString());
			}
		});
		intAddChild(themeList);
		intAddChild(new Label("Preview",curses(),new Position(1,11), new Dimension(1, 10)));
		
		preview = new ThemePreview(curses(), new Position(2,11));
		intAddChild(preview);
		
		Button use = new Button("Use",curses(),new Position(12,2),9);
		use.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repository.useTheme(getOwner().getWindowManager(),themeList.selectedItem().toString());
			}
		});
		
		intAddChild(use);
	}
}
