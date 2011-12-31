package vtui.bbs.apps.files;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import textmode.curses.application.Application;
import textmode.curses.application.Screen;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.Button;
import textmode.curses.ui.components.Label;
import textmode.curses.ui.components.LineEdit;
import textmode.curses.ui.components.MenuItem;
import textmode.curses.ui.components.MessageBox;
import textmode.curses.ui.components.ModalWindow;
import textmode.curses.ui.components.PopUp;
import textmode.curses.ui.components.Window;
import textmode.curses.ui.components.MessageBox.ButtonType;
import textmode.curses.ui.components.MessageBox.Result;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;
import textmode.util.FileAdapter;
import textmode.xfer.ZModem;
import vtui.bbs.apps.login.DomainParameters;
import vtui.bbs.apps.login.UserPrincipal;
import vtui.bbs.apps.pic.PictureViewer;
import vtui.bbs.apps.textedit.TextEditor;


public class Files extends Application {
	
	private class EnterStringDialog extends ModalWindow{
		private MessageBox.Result myResult;
		private LineEdit edit;
		
		public EnterStringDialog(String title,String prompt) {
			super(title, Files.this, Files.this.curses(), nextPosition(), new Dimension(5,20));
			setMaxSize(getSize());
			setMinSize(getSize());
			initComponents(prompt);
			notifyDisplayChange();
		}

		private void initComponents(String prompt) {
			Label lbl = new Label(prompt,curses(),new Position(1,2),new Dimension(1,getSize().getCols()-2));
			intAddChild(lbl);
			edit = new LineEdit(curses(), new Position(2,1), getSize().getCols()-2);
			intAddChild(edit);

			Button bok = new Button("Ok",curses(),new Position(3,1),8);
			Button bcancel = new Button("Cancel",curses(),new Position(3,10),8);
		
			bok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myResult = Result.OK;
					close();
				}
			});
		
			bcancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myResult = Result.CANCEL;
					close();
				}
			});
		
			intAddChild(bok);
			intAddChild(bcancel);
		}

  
		public Result waitForChoice() throws InterruptedException{
			modalWait();
			return myResult;
		}
		
		public String getText(){
			return edit.getText();
		}
		
	}
	
	private DomainParameters domain;
	private FileClipBoard cb = new FileClipBoard();
	private UserPrincipal logonUser;
	
	public Files(UserPrincipal user){
		logonUser = user;
		domain = Screen.currentSession().getAsChecked("BBSDomain", DomainParameters.class);
	}
	
	
	public UserPrincipal user(){
		return logonUser;
	}
	
	@Override
	public void stop() {

	}
	
	private void newWindow(){
		 
		try {
			FileBrowserWindow myWindow = new FileBrowserWindow(new URL(domain.filesBase),this, 
					                                curses(),nextPosition(),getWindowManager().percentOfScreen(0.5));
			showWindow(myWindow);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
				
	}
	
	private boolean confirm(String prompt){
		MessageBox dialog = new MessageBox("Are you sure?",prompt,ButtonType.YES_NO_CANCEL,this,curses());
		showWindow(dialog);
		try{
			Result r = dialog.waitForChoice();
			if(r == Result.YES)
				return true;
		}catch(InterruptedException e){e.printStackTrace();}
		
		return false;
	}
	
	private String getInput(String prompt){
		EnterStringDialog dialog = new EnterStringDialog("Question",prompt);
		showWindow(dialog);
		try{
			Result r = dialog.waitForChoice();
			if(r == Result.OK)
				return dialog.getText();
		}catch(InterruptedException e){e.printStackTrace();}
		
		return null;
	}
	
	private FileBrowserWindow intGetTopWin(){
		Window win = topMostWindow();
		if(win instanceof FileBrowserWindow){
			return ((FileBrowserWindow) win);
		}
		return null;
	}
	
	private void copy(){
		FileBrowserWindow win = intGetTopWin();
		
		cb.put(win.getFs().absPath(win.selectedFileName()));
	}
	
	private String dialogException(Exception e){
		return e.getClass()+"\n"+e.getMessage();
	}
	
	private void paste(){
		FileBrowserWindow win = intGetTopWin();
		
		String n= cb.get();
		
		if(n!=null){
			try{
				if(!win.getFs().copyToCwd(n))
					MessageBox.informUser("Paste", "Operation failed", Files.this, curses());
			}catch(Exception e){
				MessageBox.informUser("Paste", "Operation failed\n"+dialogException(e), Files.this, curses());				
			}
			win.refreshFiles();
		}
	}
	
	private void delete(){
		FileBrowserWindow win = intGetTopWin();
		
		if(confirm("Delete?\n"+win.selectedFileName())){
		
			if(!win.getFs().delete(win.selectedFileName()))
				MessageBox.informUser("Delete", "Operation failed", Files.this, curses());
			else
				win.refreshFiles();
			
		}
	}
	
	private void mkFile(){
		FileBrowserWindow win = intGetTopWin();
			
		String n = getInput("Name?");
		if(n!=null){
			
			try{
				if(!win.getFs().create(n))
					MessageBox.informUser("Create file", "Operation failed", Files.this, curses());
			}catch(Exception e){
				MessageBox.informUser("Create file", "Operation failed\n"+dialogException(e), Files.this, curses());				
			}
			
			win.refreshFiles();
		}

	}
	private void mkDir(){
		FileBrowserWindow win = intGetTopWin();
		
		String n = getInput("Name?");
		if(n!=null){
			if(!win.getFs().mkdir(n))
				MessageBox.informUser("Create direcotry", "Operation failed", Files.this, curses());
			else
				win.refreshFiles();
		}
	}
	
	@Override
	public void start() {
		newWindow();
		
		PopUp window = getWindowManager().newPopUp(30);
		
		
		MenuItem nw = new MenuItem("New Window", curses());
		window.addItem(nw);
		nw.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newWindow();
			}
		});
		
		
		
		getMenuBar().addPopUp("Window", window);
		
		PopUp file = getWindowManager().newPopUp(30);
		
		MenuItem edit = new MenuItem("Edit file", curses());
		file.addItem(edit);
		
		edit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editFile();
			}
		});
		
		MenuItem prev = new MenuItem("Preview picture", curses());
		file.addItem(prev);
		
		prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				previewPicture();
			}
		});
		
		MenuItem mkd = new MenuItem("Create directory", curses());
		file.addItem(mkd);
		
		mkd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mkDir();
			}
		});
		
		
		MenuItem mkf = new MenuItem("Create file", curses());
		file.addItem(mkf);
		
		mkf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mkFile();
			}
		});
		
		MenuItem cp = new MenuItem("Copy", curses());
		file.addItem(cp);
		
		cp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copy();
			}
		});
		
		MenuItem ps = new MenuItem("Paste", curses());
		file.addItem(ps);
		
		ps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paste();
			}
		});
		
		MenuItem del = new MenuItem("Delete", curses());
		file.addItem(del);
		
		del.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delete();
			}
		});
		
		getMenuBar().addPopUp("Filesystem", file);
		
		PopUp zm = getWindowManager().newPopUp(30);
		
		MenuItem zsend = new MenuItem("Download", curses());
		zm.addItem(zsend);
		
		zsend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileBrowserWindow win = intGetTopWin();
				if(win==null) return;
				
				ZModem zmdm = getWindowManager().createZModem();
				getWindowManager().suspend();
				try{
					zmdm.send(win.getFs().getTree(win.selectedFileName()));
				}catch(IOException ioe){
					ioe.printStackTrace();
				}
				win.refreshFiles();
				getWindowManager().resume();
			}
		});
		
		MenuItem zrecv = new MenuItem("Upload", curses());
		zm.addItem(zrecv);
		
		zrecv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileBrowserWindow win = intGetTopWin();
				if(win==null) return;
				
				ZModem zmdm = getWindowManager().createZModem();
				getWindowManager().suspend();
				try{
					zmdm.receive(win.getFs().getFile(""));
				}catch(IOException ioe){
					ioe.printStackTrace();
				}
				win.refreshFiles();
				getWindowManager().resume();
			}
		});
		
		getMenuBar().addPopUp("ZModem", zm);

	}
	

	private void previewPicture() {
		FileBrowserWindow win = intGetTopWin();
		if(win!=null){
			FileAdapter f = win.getFs().getFile(win.selectedFileName());
			getWindowManager().getApplication(PictureViewer.class).openFile(f);
		}
	}

	private void editFile() {
		FileBrowserWindow win = intGetTopWin();
		if(win!=null){
			FileAdapter f = win.getFs().getFile(win.selectedFileName());
			getWindowManager().getApplication(TextEditor.class).openFile(f);
		}
	}


	@Override
	protected String name() {
		return "Files";
	}

}
