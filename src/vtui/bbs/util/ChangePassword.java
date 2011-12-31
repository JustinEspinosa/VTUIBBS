package vtui.bbs.util;

import java.io.IOException;
import java.util.HashMap;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

import textmode.curses.application.Screen;
import vtui.bbs.apps.login.DomainParameters;
import vtui.bbs.apps.login.UserPrincipal;
import vtui.bbs.util.domain.AppleSasl;
import vtui.bbs.util.domain.LdapData;


public final class ChangePassword {
	
	public static boolean setPassword(UserPrincipal principal, String oldpwd,String password) {
		CallbackHandler nullcb = new CallbackHandler() {
			public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {				
			}
		};
		
		DomainParameters domain = Screen.currentSession().getAsChecked("BBSDomain", DomainParameters.class);
		LdapData ldap = new LdapData(domain.ldapHost, domain.ldapBaseDN);
		AppleSasl sasl = new AppleSasl(domain.appleSaslHost,Integer.parseInt(domain.appleSaslPort));
		try{
			if(sasl.connect()){
				if(sasl.beginCASTEncryption()){
					String uid = ldap.getPWServerUserID(principal.getShortName());
					HashMap<String, String> props = new HashMap<String, String>();
					props.put("javax.security.sasl.qop", "auth-conf");
					SaslClient client =  Sasl.createSaslClient(new String[]{"GSSAPI"}, null, "rcmd", "scrooge.duck.home", props, nullcb);
					if(sasl.authSASL(client, uid))
					//if(sasl.authDigestMd5(uid, oldpwd)) <-- less cool than GSSAPI
						return sasl.changepass(uid, oldpwd, password);
				}
			}
		}catch(SaslException e){
			e.printStackTrace();
		}finally{
			sasl.disconnect();
		}
		return false;
	}
}
