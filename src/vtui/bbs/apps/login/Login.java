package vtui.bbs.apps.login;


import javax.security.auth.login.LoginException;

import textmode.curses.application.RootApplication;
import textmode.curses.application.Screen;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.Position;
import textmode.curses.ui.Rectangle;
import textmode.curses.ui.UiEventProcessor;
import textmode.curses.ui.UiEventProcessorFactory;
import textmode.curses.ui.components.Button;
import textmode.curses.ui.components.Label;
import textmode.curses.ui.components.LineEdit;
import textmode.curses.ui.components.MenuItem;
import textmode.curses.ui.components.MessageBox;
import textmode.curses.ui.components.PopUp;
import textmode.curses.ui.components.RootPlane;
import textmode.curses.ui.components.Window;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;
import textmode.curses.ui.event.RedrawEvent;
import textmode.curses.ui.event.UiEvent;
import vtui.bbs.apps.chat.Chat;
import vtui.bbs.apps.chat.ChatApplicationFactory;
import vtui.bbs.apps.files.Files;
import vtui.bbs.apps.files.FilesApplicationFactory;
import vtui.bbs.apps.look.LnFFactory;
import vtui.bbs.apps.look.LookNFeel;
import vtui.bbs.apps.look.ThemeRepository;
import vtui.bbs.apps.textedit.TextEditor;
import vtui.bbs.apps.textedit.TextEditorFactory;
import vtui.bbs.apps.userlist.UserList;
import vtui.bbs.apps.userlist.UserListApplicationFactory;
import vtui.bbs.session.BBSSession;
import vtui.bbs.session.BBSSessionUtils;

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
	private BBSSession    session;
	
	public Login(){
	}	
	
	@Override
	public void stop() {
		BBSSessionUtils.saveSession(session);
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
			
			session = BBSSessionUtils.loadSession(logonUser.getName());
			Screen.currentSession().put("BBSSession", session);
			
			if(session.uiTheme.length()>0){
				try{
					ThemeRepository repository = new ThemeRepository();
					repository.useTheme(getWindowManager(), session.uiTheme);
					//This task is not important. Eat all the exceptions.
				}catch(Exception exp){}
			}
			
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
			
			LnFFactory lnf = new LnFFactory();
			getWindowManager().addToAppPool(LookNFeel.class,lnf);
			getWindowManager().submitApplicationToMenu(lnf);	
			
			startBBS();
			super.start();
			getWindowManager().receiveEvent(new RedrawEvent(this, new Rectangle(new Position(0, 0), getMenuBar().getSize())));
		}
		
	}
	
	

}
