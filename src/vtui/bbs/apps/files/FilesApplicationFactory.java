package vtui.bbs.apps.files;

import vtui.bbs.apps.login.UserPrincipal;
import fun.useless.curses.application.Application;
import fun.useless.curses.application.SingleInstanceApplicationFactory;

public class FilesApplicationFactory extends SingleInstanceApplicationFactory {

	private UserPrincipal logonUser;
	
	public FilesApplicationFactory(UserPrincipal user){
		logonUser = user;
	}
	
	@Override
	public String getDisplayName() {
		return "Files";
	}

	@Override
	public Application newInstance() {
		return new Files(logonUser);
	}

}
