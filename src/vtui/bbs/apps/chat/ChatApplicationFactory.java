package vtui.bbs.apps.chat;

import textmode.curses.application.Application;
import textmode.curses.application.SingleInstanceApplicationFactory;
import vtui.bbs.apps.login.UserPrincipal;

public class ChatApplicationFactory extends SingleInstanceApplicationFactory {

	private static final ChatServer server = new ChatServer();
	private UserPrincipal token;
	
	public ChatApplicationFactory(UserPrincipal tok){
		token=tok;
	}
	
	@Override
	public String getDisplayName() {
		return "Chat";
	}

	@Override
	public Application newInstance() {
		return new Chat(server,token);
	}



}
