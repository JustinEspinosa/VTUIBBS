package vtui.bbs.apps.files;

import java.net.URL;

import textmode.curses.Curses;
import textmode.curses.application.Application;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.Button;
import textmode.curses.ui.components.Label;
import textmode.curses.ui.components.Window;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;
import textmode.curses.ui.event.SelectionChangeEvent;
import textmode.curses.ui.event.SelectionChangedListener;
import vtui.bbs.util.FileBrowser;
import vtui.bbs.util.FileSystemAdapter;

public class FileBrowserWindow extends Window {
	
	private FileBrowser browser;
	private Button      upOneLevel;
	private Label       statusBar;

	public FileBrowserWindow(URL url, Application app, Curses cs,Position p, Dimension d) throws InstantiationException, IllegalAccessException {
		super("Files", app, cs, p, d);
		initComponents(url);
		notifyDisplayChange();
	}
	
	public FileSystemAdapter getFs(){
		return browser.getFs();
	}
	
	private void initComponents(URL url) throws InstantiationException, IllegalAccessException{
		browser = new FileBrowser(url, curses(), new Position(2,0), getSize().vertical(-3));
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
	
	
	@Override
	protected void userResized() {
		browser.setSize(getSize().vertical(-3));
		statusBar.setPosition(new Position(getSize().getLines()-1,0));
		statusBar.setSize(new Dimension(1,getSize().getCols()));
	}

	public void refreshFiles() {
		browser.reload();
	}

	public String selectedFileName() {
		return browser.selectedItem().toString();
	}

}
