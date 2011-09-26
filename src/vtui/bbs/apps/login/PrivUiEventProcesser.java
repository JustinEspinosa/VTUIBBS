package vtui.bbs.apps.login;

import java.security.PrivilegedAction;

import fun.useless.curses.ui.UiEventProcessor;
import fun.useless.curses.ui.components.RootPlane;
import fun.useless.curses.ui.event.UiEvent;

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
				PrivUiEventProcesser.super.run();
				return null;
			}
		});
		
	}
}
