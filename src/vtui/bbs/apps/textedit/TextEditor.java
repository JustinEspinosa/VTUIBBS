package vtui.bbs.apps.textedit;

import java.io.File;

import fun.useless.curses.application.Application;
import fun.useless.curses.ui.Dimension;
import fun.useless.curses.ui.components.MenuItem;
import fun.useless.curses.ui.components.PopUp;
import fun.useless.curses.ui.components.Window;
import fun.useless.curses.ui.event.ActionEvent;
import fun.useless.curses.ui.event.ActionListener;

public class TextEditor extends Application {

	@Override
	public void stop() {

	}

	@Override
	public void start() {
		buildMenu();
	}
	
	private void buildMenu(){
		MenuItem tmpItem;
		PopUp fMenu = getWindowManager().newPopUp(30);
		tmpItem = new MenuItem("New",curses());
		fMenu.addItem(tmpItem);
		
		tmpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newWindow();
			}
		});
		
		
		
		tmpItem = new MenuItem("Save",curses());
		fMenu.addItem(tmpItem);
		tmpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		
		
		
		
		getMenuBar().addPopUp("File", fMenu);
	}
	
	private TextEditorWindow newWindow(){
		return newWindow("New");
	}
	private TextEditorWindow newWindow(String title){
		TextEditorWindow newWin = new TextEditorWindow(title, this,curses(), getWindowManager().getNextWindowPosition(), new Dimension(15, 30));
		showWindow(newWin);	
		return newWin;
	}

	@Override
	protected String name() {
		return "Edit";
	}
	
	private TextEditorWindow intGetTopWin(){
		Window win = topMostWindow();
		if(win instanceof TextEditorWindow){
			return ((TextEditorWindow) win);
		}
		return null;
	}
	
	private void save(){
		TextEditorWindow win = intGetTopWin();
		if(win!=null)
			win.save();
	}
	
	public void openFile(File f){
		TextEditorWindow win = newWindow(f.getName());
		win.load(f);
	}

}
