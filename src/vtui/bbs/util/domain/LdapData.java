package vtui.bbs.util.domain;


import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LdapData {
	
	private String ldapHost;
	private String searchBase;

	public LdapData( String host, String dn){
		this.ldapHost = host;
		this.searchBase = dn;
	}
	
	private static String APPLE_PASSWORD_SERVER = "ApplePasswordServer";
	@SuppressWarnings("unchecked")
	public String getPWServerAuthority(String user){
		String returnedAtts[] = { "authAuthority" };
		Map<String,Object> attrs =  getAttrs(user,returnedAtts);
		Object o = attrs.get("authAuthority");
		if(o instanceof NamingEnumeration<?>){
			try{
				NamingEnumeration<String> e = (NamingEnumeration<String>)o;
				while(e.hasMore()){
					String val = e.next();
					int i = val.indexOf(APPLE_PASSWORD_SERVER);
					if(i>=0){
						return val;
					}
				}
			}catch(NamingException ne){
				ne.printStackTrace();
			}
		}
		return null;
	}

	public String getPWServerUserID(String user){
		String val = getPWServerAuthority(user);
		if(val!=null){
			int  i = val.indexOf(APPLE_PASSWORD_SERVER);
			String sub = val.substring(i+APPLE_PASSWORD_SERVER.length()+1);
			return sub.substring(0,sub.indexOf(","));
		}
		return user;
	}

	public Map<String,Object> getFullName(String user){
		String returnedAtts[] ={ "sn", "givenName" };
		return getAttrs(user,returnedAtts);
	}
  
	@SuppressWarnings("unchecked")
	public String getServiceInfo(String computer){
		String returnedAtts[] = { "apple-serviceinfo" };
		Map<String,Object> attrs =  getComputerAttrs(computer,returnedAtts);
		Object o = attrs.get("apple-serviceinfo");
		if(o instanceof NamingEnumeration<?>){
			try{
				NamingEnumeration<String> e = (NamingEnumeration<String>)o;
				if(e.hasMore())
					return e.next();
			}catch(NamingException ne){
				ne.printStackTrace();
			}
			return null;
		}else{
			return o.toString();
		}
		
	}
	
	private Map<String,Object> getComputerAttrs(String computer, String[] returnedAtts){
		String searchFilter = "(&(objectClass=apple-computer)(cn=" + computer + "))";
		List<Map<String,Object>> list = searchLdap(searchFilter,returnedAtts);
		if(list.size()>0)
			return list.get(0);
		
		return null;
	}
  
	
	private Map<String,Object> getAttrs(String user, String[] returnedAtts){
		String searchFilter = "(&(objectClass=person)(uid=" + user + "))";
		List<Map<String,Object>> list = searchLdap(searchFilter,returnedAtts);
		if(list.size()>0)
			return list.get(0);
		
		return null;
	}
  
	public List<Map<String,Object>> getUserList(){
	  
		String searchFilter = "(&(objectClass=apple-user))";
		String returnedAtts[] ={ "sn", "givenName", "uid" };
		  
		return searchLdap(searchFilter,returnedAtts);
	}  
	
  
	private List<Map<String,Object>> searchLdap(String searchFilter, String[] returnedAtts){

		//Create the search controls
		SearchControls searchCtls = new SearchControls();
		searchCtls.setReturningAttributes(returnedAtts);
		searchCtls.setCountLimit(0);
		
		//Specify the search scope
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		
		Hashtable<String,String> env = new Hashtable<String,String> ();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapHost);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		/*env.put(Context.SECURITY_PRINCIPAL, user);
		env.put(Context.SECURITY_CREDENTIALS, pass);*/
		
		LdapContext ctxGC = null;
		
		try{
			ctxGC = new InitialLdapContext(env, null);
			//Search objects in GC using filters
			NamingEnumeration<SearchResult> answer = ctxGC.search(searchBase, searchFilter, searchCtls);
			List<Map<String,Object>> amaplist = new Vector<Map<String,Object>>();
			while (answer.hasMoreElements()){
			  
				SearchResult sr = (SearchResult) answer.next();
				Attributes attrs = sr.getAttributes();
		    
				if (attrs != null){
					Map<String,Object> amap = new HashMap<String,Object>();
					NamingEnumeration<? extends Attribute> ne = attrs.getAll();
		    	
					while (ne.hasMore()){
						Attribute attr = ne.next();
						if(attr.size()==1)
							amap.put(attr.getID(), attr.get());
						else
							amap.put(attr.getID(), attr.getAll());
					}
		      
					ne.close();
					amaplist.add(amap);
				}
			}
			return amaplist;
      
    	}catch (NamingException ex){
    		ex.printStackTrace();
		}

		return null;
	}
}
