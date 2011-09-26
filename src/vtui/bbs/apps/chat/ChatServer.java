package vtui.bbs.apps.chat;

import java.util.Enumeration;
import java.util.Vector;

public class ChatServer {
	private Vector<ChatClient> clients = new Vector<ChatClient>();
	
	public synchronized void registerClient(ChatClient cli){
		clients.add(cli);
		broadcast("**"+cli.getUserName()+"**","Connected.");
	}
	
	public synchronized void unregisterClient(ChatClient cli){
		clients.remove(cli);
		broadcast("**"+cli.getUserName()+"**","Disconnected.");
	}
	
	public String[] getUsers(){
		String[] users = new String[clients.size()];
		Enumeration<ChatClient> en = clients.elements();
		int index=0;
		while(en.hasMoreElements()){
			users[index++] = en.nextElement().getUserName();
		}
		return users;
	}
	
	private void broadcast(String name,String message){
		Enumeration<ChatClient> en = clients.elements();
		while(en.hasMoreElements()){
			en.nextElement().receive(name, message);
		}
	}

	public synchronized void message(String name,String message){
		broadcast(name,message);
	}
	
}
