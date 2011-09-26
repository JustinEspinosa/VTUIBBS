package vtui.bbs.apps.chat;

public interface ChatClient {
	public String getUserName();
	public void receive(String user,String message);
}
