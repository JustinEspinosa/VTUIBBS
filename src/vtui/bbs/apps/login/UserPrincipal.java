package vtui.bbs.apps.login;

import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;


public class UserPrincipal implements Principal{

	private static class Krbv5CbHandler implements CallbackHandler{

		private String username;
		private String password;
		
		public Krbv5CbHandler(String user,String pwd){
			username = user;
			password = pwd;
		}
		
		public void handle(Callback[] callbacks) throws IOException,UnsupportedCallbackException {

			for (int i = 0; i < callbacks.length; i++) {
				
				if (callbacks[i] instanceof NameCallback) {
				    NameCallback cb = (NameCallback)callbacks[i];
				    cb.setName(username);
				}
				
				if (callbacks[i] instanceof PasswordCallback) {
				    PasswordCallback cb = (PasswordCallback)callbacks[i];

				    char[] passwd = new char[password.length()];
				    password.getChars(0, passwd.length, passwd, 0);

				    cb.setPassword(passwd);
				}
				
			}
		}
		
	}
	
	
	public static UserPrincipal authorize(String username,String password){
		
		try {
			LoginContext ctx = new LoginContext(UserPrincipal.class.getName(),new Subject(), new Krbv5CbHandler(username, password));
			ctx.login();
			return new UserPrincipal(ctx);
		} catch (LoginException e) {
			//e.printStackTrace();
			System.out.println("Failed attempt: "+username);
			return null;
		}
		
	}
	
	
	private LoginContext context;
	private Principal realPrincipal;
	
	public UserPrincipal(LoginContext ctx){
		context = ctx;
		realPrincipal = ctx.getSubject().getPrincipals().iterator().next();
	}
	
	public LoginContext context(){
		return context;
	}

	public String getName() {
		return realPrincipal.getName();
	}
	
	public String getShortName() {
		String n = getName();
		int pos = n.indexOf('@');
		if (pos>0) return n.substring(0,pos);
		      else return n;
	}
	
	public Subject subject(){
		return context().getSubject();
	}
	
	public <T> T does(PrivilegedAction<T> action){
		return Subject.doAs(context().getSubject(),action);
	}
	
	public boolean changePassword(String oldp,String newp){
		return false;		
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
