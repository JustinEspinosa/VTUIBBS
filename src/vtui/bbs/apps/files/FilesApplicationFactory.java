package vtui.bbs.apps.files;

import textmode.curses.application.Application;
import textmode.curses.application.SingleInstanceApplicationFactory;
import vtui.bbs.apps.login.UserPrincipal;

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
