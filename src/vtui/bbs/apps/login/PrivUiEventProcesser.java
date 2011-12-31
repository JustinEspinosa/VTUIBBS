package vtui.bbs.apps.login;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.PrivilegedAction;

import textmode.curses.ui.UiEventProcessor;
import textmode.curses.ui.components.MessageBox;
import textmode.curses.ui.components.RootPlane;
import textmode.curses.ui.event.UiEvent;


public class PrivUiEventProcesser extends UiEventProcessor {
	UserPrincipal logonUser;

	private static class Summarizer extends OutputStream{
		private StringBuilder data = new StringBuilder();
		private int lines = 0;
		private int maxLines = 10;
		public Summarizer(int lines) {
			maxLines = lines;
		}
		@Override
		public void write(int b) throws IOException {
			if(b == '\r' || b=='\n')
				++lines;
			if(b == '\t' )
				b = ' ';
			
			if(lines<maxLines)
				data.append((char)b);
			
			System.err.print((char)b);
		}
		public String toString(){
			data.append("...");
			System.err.print('\n');
			return data.toString();
		}
	}
	
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
					Summarizer s = new Summarizer(8);
					e.printStackTrace(new PrintStream(s));
					
					MessageBox.informUser("Unknwon error", s.toString(), app(), curses());
				}
				return null;
			}
		});
		
	}
}
