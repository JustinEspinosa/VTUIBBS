package vtui.bbs.apps.login;

import java.security.PrivilegedAction;

import textmode.curses.ui.UiEventProcessor;
import textmode.curses.ui.components.MessageBox;
import textmode.curses.ui.components.RootPlane;
import textmode.curses.ui.event.UiEvent;


public class PrivUiEventProcesser extends UiEventProcessor {
	UserPrincipal logonUser;

	public PrivUiEventProcesser(UserPrincipal user,UiEvent e, RootPlane<?> plane) {
		super(e, plane);
		logonUser = user;
	}

	@Override
	public void run() {
		logonUser.does(new PrivilegedAction<Object>() {
			public Object run() {
				try{
					PrivUiEventProcesser.super.run();
				}catch(Exception e){
					MessageBox.informUser("Unknwon error", "Operation failed:\n"+e.getClass().getName()+"\n"+e.getMessage(), app(), curses());
				}
				return null;
			}
		});
		
	}
}
