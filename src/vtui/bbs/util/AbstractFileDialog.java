package vtui.bbs.util;

import java.net.MalformedURLException;
import java.net.URL;

import textmode.curses.Curses;
import textmode.curses.application.Application;
import textmode.curses.application.Screen;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.Button;
import textmode.curses.ui.components.Label;
import textmode.curses.ui.components.MessageBox;
import textmode.curses.ui.components.ModalWindow;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;
import textmode.curses.ui.event.SelectionChangeEvent;
import textmode.curses.ui.event.SelectionChangedListener;
import textmode.util.FileAdapter;
import vtui.bbs.apps.login.DomainParameters;
import vtui.bbs.util.FileBrowser;

public abstract class AbstractFileDialog extends ModalWindow {
	
	private FileBrowser browser;
	private Button      upOneLevel;
	private Label       statusBar;
	private MessageBox.Result myResult;
	
	public AbstractFileDialog(String title, Application app, Curses cs,Position p) {
		super(title, app, cs, p, new Dimension(15,40));
		setResizeable(false);
		try {
			DomainParameters domain = Screen.currentSession().getAsChecked("BBSDomain", DomainParameters.class);
			initComponents(new URL(domain.filesBase));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public FileSystemAdapter getFs(){
		return browser.getFs();
	}
	
	private void initComponents(URL url) throws InstantiationException, IllegalAccessException{
		browser = new FileBrowser(url, curses(), new Position(2,0), getSize().vertical(-5));
		upOneLevel = new Button("..", curses(), new Position(1,0),4);
		statusBar  = new Label("",curses(),new Position(getSize().getLines()-4,0),new Dimension(1,getSize().getCols()));
		
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
	
	protected FileBrowser browser(){
		return browser;
	}
	
	protected int buttonsLine(){
		return getSize().getLines()-2;
	}
	
	protected void setResult(MessageBox.Result v){
		myResult = v;
	}

	public abstract FileAdapter getSelectedFile();
	
	public MessageBox.Result getResult(){
		return myResult;
	}
	
	public MessageBox.Result waitForChoice() throws InterruptedException{
		modalWait();
		return getResult();
	}
	
	public void refreshFiles() {
		browser.reload();
	}


}
