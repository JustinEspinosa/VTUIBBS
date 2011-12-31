package vtui.bbs.apps.pic;

import java.io.IOException;

import textmode.curses.application.Application;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.components.MenuItem;
import textmode.curses.ui.components.MessageBox.Result;
import textmode.curses.ui.components.PopUp;
import textmode.curses.ui.components.Window;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;
import textmode.util.FileAdapter;
import vtui.bbs.util.FileOpenDialog;


public class PictureViewer extends Application {

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

		tmpItem = new MenuItem("Open",curses());
		fMenu.addItem(tmpItem);
		
		tmpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				open();
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
	

	private PictureViewerWindow newWindow(FileAdapter file) throws IOException{
		
		PictureViewerWindow newWin = new PictureViewerWindow(file, this,curses(), getWindowManager().getNextWindowPosition(), new Dimension(18, 50));
		showWindow(newWin);	
		return newWin;
		
	}

	@Override
	protected String name() {
		return "Pics";
	}
	
	private PictureViewerWindow intGetTopWin(){
		Window win = topMostWindow();
		if(win instanceof PictureViewerWindow){
			return ((PictureViewerWindow) win);
		}
		return null;
	}
	
	private void closeCurrent(){
		PictureViewerWindow win = intGetTopWin();
		if(win!=null)
			win.close();
	}
	

	private void open(){
		FileOpenDialog dialog = FileOpenDialog.trySelect("Open",this, curses());
		if(dialog.getResult() == Result.OK)
			openFile(dialog.getSelectedFile());
	}
	
	public void openFile(FileAdapter f){
		try{
			newWindow(f);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}
