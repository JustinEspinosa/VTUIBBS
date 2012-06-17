package vtui.bbs.util;


import textmode.curses.application.Screen;
import vtui.bbs.apps.login.DomainParameters;
import vtui.bbs.apps.login.UserPrincipal;
import vtui.bbs.util.domain.ApplePasswordService;
import vtui.bbs.util.domain.LdapData;


public final class ChangePassword {
	
	public static boolean setPassword(UserPrincipal principal, String oldpwd,String password) {

		DomainParameters domain = Screen.currentSession().getAsChecked("BBSDomain", DomainParameters.class);
		LdapData ldap = new LdapData(domain.ldapHost, domain.ldapBaseDN);
		ApplePasswordService sasl = new ApplePasswordService(domain.appleSaslHost,Integer.parseInt(domain.appleSaslPort));
		try{
			if(sasl.connect()){
				if(sasl.beginCASTEncryption()){
					String uid = ldap.getPWServerUserID(principal.getShortName());
					if(sasl.authDHX(uid,password))
						return sasl.changepass(uid,password);
				}
			}
		}finally{
			sasl.disconnect();
		}
		return false;
	}
}
