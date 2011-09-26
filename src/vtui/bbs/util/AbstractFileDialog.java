package vtui.bbs.util;

import vtui.bbs.util.FileBrowser;
import fun.useless.curses.Curses;
import fun.useless.curses.application.Application;
import fun.useless.curses.ui.Dimension;
import fun.useless.curses.ui.Position;
import fun.useless.curses.ui.components.Button;
import fun.useless.curses.ui.components.Label;
import fun.useless.curses.ui.components.ModalWindow;
import fun.useless.curses.ui.event.ActionEvent;
import fun.useless.curses.ui.event.ActionListener;
import fun.useless.curses.ui.event.SelectionChangeEvent;
import fun.useless.curses.ui.event.SelectionChangedListener;

public class AbstractFileDialog extends ModalWindow {
	
	private FileBrowser browser;
	private Button      upOneLevel;
	private Label       statusBar;

	public AbstractFileDialog(String path, Application app, Curses cs,Position p) {
		super("Files", app, cs, p, new Dimension(30,15));
		initComponents(path);
		notifyDisplayChange();
	}
	
	public FileSystemAdapter getFs(){
		return browser.getFs();
	}
	
	private void initComponents(String path){
		browser = new FileBrowser(path, curses(), new Position(2,0), getSize().vertical(-3));
		upOneLevel = new Button("..", curses(), new Position(1,0),4);
		statusBar  = new Label("",curses(),new Position(getSize().getLines()-1,0),new Dimension(1,getSize().getCols()));
		
		intAddChild(browser);
		intAddChild(upOneLevel);
		intAddChild(statusBar);
		
		browser.addSelectionListener(new SelectionChangedListener() {
			public void selectionChanged(SelectionChangeEvent e) {
				statusBar.setText(browser.getCwd());
			}
		});
		
		upOneLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browser.upOneDir();
			}
		});
	}
	

	public void refresh() {
		browser.reload();
	}

	public String selectedFileName() {
		return browser.selectedItem().toString();
	}

}
