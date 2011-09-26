package vtui.bbs.apps.chat;

import vtui.bbs.apps.login.UserPrincipal;
import fun.useless.curses.application.Application;
import fun.useless.curses.application.SingleInstanceApplicationFactory;

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
