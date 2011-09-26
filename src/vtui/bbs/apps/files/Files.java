package vtui.bbs.apps.files;

import java.io.File;
import java.io.IOException;

import vtui.bbs.apps.login.UserPrincipal;
import vtui.bbs.apps.textedit.TextEditor;

import fun.useless.curses.application.Application;
import fun.useless.curses.ui.Dimension;
import fun.useless.curses.ui.Position;
import fun.useless.curses.ui.components.Button;
import fun.useless.curses.ui.components.Label;
import fun.useless.curses.ui.components.LineEdit;
import fun.useless.curses.ui.components.MenuItem;
import fun.useless.curses.ui.components.MessageBox;
import fun.useless.curses.ui.components.MessageBox.ButtonType;
import fun.useless.curses.ui.components.MessageBox.Result;
import fun.useless.curses.ui.components.ModalWindow;
import fun.useless.curses.ui.components.PopUp;
import fun.useless.curses.ui.components.Window;
import fun.useless.curses.ui.event.ActionEvent;
import fun.useless.curses.ui.event.ActionListener;
import fun.useless.xfer.ZModem;

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
	
	
	private FileClipBoard cb = new FileClipBoard();
	private UserPrincipal logonUser;
	
	public Files(UserPrincipal user){
		logonUser = user;
	}
	
	
	public UserPrincipal user(){
		return logonUser;
	}
	
	@Override
	public void stop() {

	}
	
	private void newWindow(){
		FileBrowserWindow myWindow = new FileBrowserWindow("/",this, curses(),nextPosition(),new Dimension(10, 40));
		showWindow(myWindow);		
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
	
	private void paste(){
		FileBrowserWindow win = intGetTopWin();
		
		String n= cb.get();
		
		if(n!=null){
			if(!win.getFs().copyToCwd(n))
				MessageBox.informUser("Paste", "Operation failed", Files.this, curses());
			else
				win.refresh();
		}
	}
	
	private void delete(){
		FileBrowserWindow win = intGetTopWin();
		
		if(confirm("Delete?\n"+win.selectedFileName())){
		
			if(!win.getFs().delete(win.selectedFileName()))
				MessageBox.informUser("Delete", "Operation failed", Files.this, curses());
			else
				win.refresh();
			
		}
	}
	
	private void mkFile(){
		FileBrowserWindow win = intGetTopWin();
			
		String n = getInput("Name?");
		if(n!=null){
			if(!win.getFs().create(n))
				MessageBox.informUser("Create file", "Operation failed", Files.this, curses());
			else
				win.refresh();
		}

	}
	private void mkDir(){
		FileBrowserWindow win = intGetTopWin();
		
		String n = getInput("Name?");
		if(n!=null){
			if(!win.getFs().mkdir(n))
				MessageBox.informUser("Create direcotry", "Operation failed", Files.this, curses());
			else
				win.refresh();
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
				win.refresh();
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
				win.refresh();
				getWindowManager().resume();
			}
		});
		
		getMenuBar().addPopUp("ZModem", zm);

	}

	private void editFile() {
		FileBrowserWindow win = intGetTopWin();
		if(win!=null){
			try {
				File f = win.getFs().getFile(win.selectedFileName());
				getWindowManager().getApplication(TextEditor.class).openFile(f);
				
			} catch (IOException e) {
				MessageBox.informUser("Error", "Unknwon error", this, curses());

			}
		}
	}


	@Override
	protected String name() {
		return "Files";
	}

}
