package vtui.bbs.apps.chat;

import textmode.curses.Curses;
import textmode.curses.application.Application;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.Button;
import textmode.curses.ui.components.LineEdit;
import textmode.curses.ui.components.ListBox;
import textmode.curses.ui.components.MultiLineEdit;
import textmode.curses.ui.components.Window;
import textmode.curses.ui.data.ListModel;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;

public class ChatWindow extends Window implements ChatClient{
	
	private class UserListModel implements ListModel{

		private String[] users;
		
		private UserListModel(){
			load();
		}
		
		public void load(){
			users = server.getUsers();
		}
		
		public int getItemCount() {
			return users.length;
		}

		public Object getItemAt(int index) {
			return users[index];
		}
		
	}

	private MultiLineEdit feed;
	private LineEdit input;
	private ChatServer server;
	private Button send;
	private UserListModel model;
	private ListBox userList;

	public ChatWindow(ChatServer s,String title, Application app, Curses cs,Position p,Dimension d) {
		super(title, app, cs,p, d);
		
		server = s;
		
		model = new UserListModel();
		
		userList = new ListBox(model, curses(), new Position(1,d.getCols()-11), new Dimension(d.getLines()-3,11));
		
		send = new Button("send",curses(),new Position(d.getLines()-2,d.getCols()-8),8);
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				post();
			}
		});
		
		feed = new MultiLineEdit(curses(),new Position(1,1),d.mutate(-3,-13) );
		feed.setReadOnly(true);
		input = new LineEdit(curses(),new Position(d.getLines()-2,1),d.getCols()-10);
		
		input.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				post();
			}
		});
		
		intAddChild(userList);
		intAddChild(feed);
		intAddChild(input);
		intAddChild(send);
		
		
		setFocusNoNotify(input);
		
	}
	
	public void receive(String user,String message){
		feed.appendLine(user + ": " + message);
		feed.srollToEnd();
		model.load();
		userList.updateFromModel();
	}

	public void post(){
		String user = getUserName();
		
		server.message(user, input.getText());
		input.setText("");
		setFocus(input);
	}

	public String getUserName() {
		String user = "Unknwon";
		if(getOwner() instanceof Chat){
			user = ((Chat)getOwner()).getUserName();
		}
		return user;
	}
	
	@Override
	protected void userResized() {
		feed.setSize(getSize().mutate(-3,-13));
		input.setPosition(new Position(getSize().getLines()-2,1));
		input.setSize(new Dimension(1,getSize().getCols()-10));
		send.setPosition(new Position(getSize().getLines()-2,getSize().getCols()-8));
		userList.setPosition(new Position(1,getSize().getCols()-11));
		userList.setSize(new Dimension(getSize().getLines()-3,11));
	}


}
