package vtui.bbs.apps.changepw;

import textmode.curses.application.Application;
import textmode.curses.application.Screen;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.Button;
import textmode.curses.ui.components.Label;
import textmode.curses.ui.components.LineEdit;
import textmode.curses.ui.components.MessageBox;
import textmode.curses.ui.components.ModalWindow;
import textmode.curses.ui.components.MessageBox.Result;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;
import vtui.bbs.apps.login.UserPrincipal;


public class ChangePassword extends Application {

	private class ChangePasswordDialog extends ModalWindow{
		private MessageBox.Result myResult;
		private LineEdit oldPwd;
		private LineEdit pwd1;
		private LineEdit pwd2;
		private String password;
		private String oldPassword;
		
		
		public ChangePasswordDialog() {
			super("Change password", ChangePassword.this, ChangePassword.this.curses(), nextPosition(), new Dimension(14,25));
			setMaxSize(getSize());
			setMinSize(getSize());
			initComponents();
			notifyDisplayChange();
		}

		private void initComponents() {
			Label lbl = new Label("Enter the old password",curses(),new Position(2,1),new Dimension(1,getSize().getCols()-2));
			intAddChild(lbl);
			oldPwd = new LineEdit(curses(), new Position(4,1), getSize().getCols()-2);
			oldPwd.setReplacementChar('*');
			intAddChild(oldPwd);
			lbl = new Label("Enter the new\npassword twice",curses(),new Position(6,1),new Dimension(2,getSize().getCols()-2));
			intAddChild(lbl);
			pwd1 = new LineEdit(curses(), new Position(8,1), getSize().getCols()-2);
			pwd1.setReplacementChar('*');
			intAddChild(pwd1);
			pwd2 = new LineEdit(curses(), new Position(10,1), getSize().getCols()-2);
			pwd2.setReplacementChar('*');
			intAddChild(pwd2);
			
			Button bok = new Button("Ok",curses(),new Position(12,1),8);
			Button bcancel = new Button("Cancel",curses(),new Position(12,10),8);
		
			bok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(check()){
						myResult = Result.OK;
						close();
					}
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
		
		private boolean check(){
			oldPassword = oldPwd.getText();
			String p1 = pwd1.getText();
			String p2 = pwd2.getText();
			if(p1.equals(p2)){
				password = p1;
				return true;
			}
			MessageBox.informUser("New password", "The passwords are different.", getOwner(), curses());
			return false;
		}

  
		public Result waitForChoice() throws InterruptedException{
			modalWait();
			return myResult;
		}
		public String getOldPassword(){
			return oldPassword;
		}
		public String getPassword(){
			return password;
		}
		
	}
	
	@Override
	public void stop() {
	}

	@Override
	public void start() {
		ChangePasswordDialog dlg = new ChangePasswordDialog();
		
		showWindow(dlg);
		try {
			if(dlg.waitForChoice()==Result.OK){
				if(!vtui.bbs.util.ChangePassword.setPassword(Screen.currentSession().getAsChecked("UserPrincipal", UserPrincipal.class),dlg.getOldPassword(), dlg.getPassword()))
					MessageBox.informUser("Failed", "Sorry", this, curses());
				else
					MessageBox.informUser("It worked", "Unexpectedly...", this, curses());
			}
		} catch (InterruptedException e) {
		}
		end();
	}

	@Override
	protected String name() {
		return "Change password";
	}

}
