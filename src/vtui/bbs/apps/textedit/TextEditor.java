package vtui.bbs.apps.textedit;

import java.io.File;

import textmode.curses.application.Application;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.components.MenuItem;
import textmode.curses.ui.components.PopUp;
import textmode.curses.ui.components.Window;
import textmode.curses.ui.components.MessageBox.Result;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;
import vtui.bbs.util.FileOpenDialog;
import vtui.bbs.util.FileSaveDialog;


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
		
		tmpItem = new MenuItem("Open",curses());
		fMenu.addItem(tmpItem);
		
		tmpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				open();
			}
		});
		
		tmpItem = new MenuItem("Save",curses());
		fMenu.addItem(tmpItem);
		tmpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		
		tmpItem = new MenuItem("Save as...",curses());
		fMenu.addItem(tmpItem);
		tmpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});
		
		
		getMenuBar().addPopUp("File", fMenu);
		
		PopUp wMenu = getWindowManager().newPopUp(30);
		tmpItem = new MenuItem("Close",curses());
		wMenu.addItem(tmpItem);
		
		tmpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeCurrent();
			}
		});
		
		getMenuBar().addPopUp("Window", wMenu);
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
	
	private void closeCurrent(){
		TextEditorWindow win = intGetTopWin();
		if(win!=null)
			win.close();
	}
	
	private void saveAs() {
		TextEditorWindow win = intGetTopWin();
		if(win==null) return;
		
		FileSaveDialog dialog = FileSaveDialog.queryFileName("Save",this, curses());
		if(dialog.getResult() == Result.OK)
			doSaveAs(win,dialog.getSelectedFile());
	}
	
	private void doSaveAs(TextEditorWindow win,File selectedFile) {
		win.saveAs(selectedFile);
	}

	private void save(){
		TextEditorWindow win = intGetTopWin();
		if(win!=null){
			if(win.canSave())
				win.save();
			else
				saveAs();
		}
	}
	
	private void open(){
		FileOpenDialog dialog = FileOpenDialog.trySelect("Open",this, curses());
		if(dialog.getResult() == Result.OK)
			openFile(dialog.getSelectedFile());
	}
	
	public void openFile(File f){
		TextEditorWindow win = newWindow(f.getName());
		win.load(f);
	}

}
