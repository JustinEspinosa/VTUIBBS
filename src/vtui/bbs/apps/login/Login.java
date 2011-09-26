package vtui.bbs.apps.login;


import javax.security.auth.login.LoginException;

import vtui.bbs.apps.chat.Chat;
import vtui.bbs.apps.chat.ChatApplicationFactory;
import vtui.bbs.apps.files.Files;
import vtui.bbs.apps.files.FilesApplicationFactory;
import vtui.bbs.apps.textedit.TextEditor;
import vtui.bbs.apps.textedit.TextEditorFactory;
import vtui.bbs.apps.userlist.UserList;
import vtui.bbs.apps.userlist.UserListApplicationFactory;
import fun.useless.curses.application.RootApplication;
import fun.useless.curses.ui.Dimension;
import fun.useless.curses.ui.Position;
import fun.useless.curses.ui.Rectangle;
import fun.useless.curses.ui.UiEventProcessor;
import fun.useless.curses.ui.UiEventProcessorFactory;
import fun.useless.curses.ui.components.Button;
import fun.useless.curses.ui.components.Label;
import fun.useless.curses.ui.components.LineEdit;
import fun.useless.curses.ui.components.MenuItem;
import fun.useless.curses.ui.components.MessageBox;
import fun.useless.curses.ui.components.PopUp;
import fun.useless.curses.ui.components.RootPlane;
import fun.useless.curses.ui.components.Window;
import fun.useless.curses.ui.event.ActionEvent;
import fun.useless.curses.ui.event.ActionListener;
import fun.useless.curses.ui.event.RedrawEvent;
import fun.useless.curses.ui.event.UiEvent;

public class Login extends RootApplication implements ActionListener{

	
	private class LoginWindow extends Window {
		
		private LineEdit name;
		private LineEdit pwd;

		public LoginWindow() {
			super("Login",Login.this,Login.this.curses(), new Position(8,30), new Dimension(9, 20));
						
			Button ok = new Button("OK",curses(),new Position(7,4),12);
			ok.addActionListener(Login.this);
			
			Label lname = new Label("Name",curses(),new Position(2,1),new Dimension(1,15) );
			name = new LineEdit(curses(),new Position(3, 1), 18);
			
			Label lpwd = new Label("Password",curses(),new Position(4,1),new Dimension(1,15) );
			pwd = new LineEdit(curses(),new Position(5, 1), 18);
			pwd.setReplacementChar('*');
			
			intAddChild(lname);
			intAddChild(name);
			intAddChild(lpwd);
			intAddChild(pwd);
			intAddChild(ok);
			
			setFocusNoNotify(name);
		}
		
		
		public String getUserName(){
			return name.getText();
		}
		public String getPassword(){
			return pwd.getText();
		}


	}
	
	private LoginWindow   prompt;
	private UserPrincipal logonUser;
	
	public Login(){
	}	
	
	@Override
	public void stop() {
		//TODO: release thingies
		
		getWindowManager().stop();
	}

	@Override
	public void start() {
		prompt = new LoginWindow();
		
		showWindow(prompt);
	}

	@Override
	protected String name() {
		return "VTUI.BBS";
	}

	public void startBBS(){
		PopUp BBSMenu = getWindowManager().newPopUp(30);
		
		MenuItem logoff = new MenuItem("Logoff",curses());
		logoff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					logonUser.context().logout();
				} catch (LoginException e1) {
					e1.printStackTrace();
				}
				stop();
			}
		});
		
		BBSMenu.addItem(logoff);
		
		getMenuBar().addPopUp("BBS", BBSMenu);
	}

	public void actionPerformed(ActionEvent e) {
		
		prompt.close();
		
		logonUser = UserPrincipal.authorize(prompt.getUserName(), prompt.getPassword());
		
		if(logonUser == null){
			MessageBox.informUser("Sorry.", "Wrong password (or username).", this,curses());
			showWindow(prompt);
			return;
		}else{
			getWindowManager().setProcessorFactory(new UiEventProcessorFactory() {
				public UiEventProcessor createProcessor(UiEvent e, RootPlane<?> plane) {
					return new PrivUiEventProcesser(logonUser, e, plane);
				}
			});
			ChatApplicationFactory chat = new ChatApplicationFactory(logonUser);
			getWindowManager().addToAppPool(Chat.class,chat);
			getWindowManager().submitApplicationToMenu(chat);
			
			UserListApplicationFactory userlist = new UserListApplicationFactory();
			getWindowManager().addToAppPool(UserList.class,userlist);
			getWindowManager().submitApplicationToMenu(userlist);
			
			FilesApplicationFactory files = new FilesApplicationFactory(logonUser);
			getWindowManager().addToAppPool(Files.class,files);
			getWindowManager().submitApplicationToMenu(files);
			
			TextEditorFactory text = new TextEditorFactory();
			getWindowManager().addToAppPool(TextEditor.class,text);
			getWindowManager().submitApplicationToMenu(text);		
			
			startBBS();
			super.start();
			getWindowManager().receiveEvent(new RedrawEvent(this, new Rectangle(new Position(0, 0), getMenuBar().getSize())));
		}
		
	}
	
	

}
