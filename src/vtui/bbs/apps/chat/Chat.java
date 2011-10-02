package vtui.bbs.apps.chat;

import textmode.curses.application.Application;
import textmode.curses.ui.Dimension;
import vtui.bbs.apps.login.UserPrincipal;

public class Chat extends Application {

	private ChatServer server;
	private ChatWindow myWindow;
	private UserPrincipal principal;
	
	public Chat(ChatServer s,UserPrincipal princ){
		server = s;
		principal = princ;
	}
	
	public String getUserName(){
		return principal.getShortName();
	}
	
	@Override
	public void stop() {
		server.unregisterClient(myWindow);
	}

	@Override
	public void start() {
		myWindow = new ChatWindow(server, "Chat", this, curses(), nextPosition(), new Dimension(18,60));
		showWindow(myWindow);
		server.registerClient(myWindow);
	}

	@Override
	protected String name() {
		return "Chat";
	}

}
