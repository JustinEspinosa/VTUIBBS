package vtui.bbs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import textmode.curses.application.Screen;
import textmode.util.FileAdapter;
import vtui.bbs.apps.login.UserPrincipal;

import jcifs.Config;
import jcifs.smb.Kerb5Authenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;


public class JCifsFileSystemAdapter implements FileSystemAdapter {
	
	private Kerb5Authenticator kerbAuth;
	private String rootUrl;
	private String cwd;
	private String root;	
	
	static{
        Config.setProperty("jcifs.smb.client.capabilities",Kerb5Authenticator.CAPABILITIES);
        Config.setProperty("jcifs.smb.client.flags2",Kerb5Authenticator.FLAGS2);
        Config.setProperty("jcifs.smb.client.signingPreferred", "true");
        FileSystem.registerAdapter("smb", JCifsFileSystemAdapter.class);
	}
	
	public JCifsFileSystemAdapter(URL url){
		
		UserPrincipal principal = Screen.currentSession().getAsChecked("UserPrincipal",UserPrincipal.class);
		kerbAuth = new Kerb5Authenticator(principal.subject());
		
		rootUrl = url.getPath();
		
		cwd    = String.valueOf(PATHSEP);
		root   = String.valueOf(PATHSEP);
	}
	
	private SmbFile realFile(String path) throws MalformedURLException, UnknownHostException{
		
		return new SmbFile(gluePathElements(rootUrl,path),kerbAuth);
	}
	
	private String upOne(String path){
		int index = path.lastIndexOf(PATHSEP);
		return path.substring(0,index+1);
	}
		
	private String reducePath(String path){
		String canonpath;
		
		if(path.length()==0)
			return new String(cwd);
		
		if(path.charAt(0)==PATHSEP)
			canonpath = new String(root);
		else
			canonpath = new String(cwd);
		
		StringTokenizer tokens = new StringTokenizer(path, String.valueOf(PATHSEP));
		
		
		while(tokens.hasMoreTokens()){
			String folder = tokens.nextToken();
			if(folder.equals("."))
				continue;
			if(folder.equals("..")){
				if(!canonpath.equals(root))
					canonpath = upOne(canonpath);
				
				continue;
			}
			if(!canonpath.equals(root))
				canonpath += PATHSEP;
			
			canonpath += folder;
		}
		
		return canonpath;
	}
	
	private void intSetCwd(String path)
	{
		cwd = new String(path);
		if(cwd.charAt(cwd.length()-1)!=PATHSEP)
			cwd += PATHSEP;
		
	}
	
	public String absPath(String file){
		return reducePath(cwd+file);
	}

	public FileAdapter getFile(String file){
		try {
			return new SmbFileAdapter(realFile(reducePath(file)),kerbAuth);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String cwd(){
		return new String(cwd);
	}
	
	private String gluePathElements(String a,String b){
		StringBuilder buff = new StringBuilder();
				
		buff.append(a);
		
		if( ! (a.endsWith("/") || b.startsWith("/")) )
			buff.append(PATHSEP);
		
		buff.append(b);
		
		return buff.toString();
		
	}
	
	private void addFile(String dir,Map<String,FileAdapter> tree) throws MalformedURLException, UnknownHostException, SmbException{
		SmbFile rf = realFile(reducePath(dir));
		
		if(rf.isDirectory()){
			String[] files = rf.list();
			for(String f: files)
				addFile(gluePathElements(dir,f),tree);
		
		}else{ 
			tree.put(dir,new SmbFileAdapter(rf, kerbAuth) ); 
		}
			
	}
	
	public Map<String,FileAdapter> getTree(String folder){
		Map<String,FileAdapter> tree = new HashMap<String,FileAdapter>();
		
		try {
			addFile(folder,tree);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (SmbException e) {
			throw new RuntimeException(e);
		}
		
		return tree;
	}
	
	public boolean copyToCwd(String from) throws IOException{
		SmbFile f1 = realFile(reducePath(from));
		SmbFile f2 = realFile(absPath(f1.getName()));
		
		try{
			f2.createNewFile();
			InputStream  is = new SmbFileInputStream(f1);
			OutputStream os = new SmbFileOutputStream(f2);
			
			int len = 0;
			byte[] data = new byte[1024];
			do{
				len = is.read(data);
				if(len>0)
					os.write(data,0,len);
			}while(len > 0);
				
			return true;
			
		}catch(SmbException e){
		}
			
		return false;
	}
	
	public boolean delete(String name){
		try{
			realFile(absPath(name)).delete();
			return true;
		} catch(SmbException e){
		} catch (MalformedURLException e) {
		} catch (UnknownHostException e) {
		}
		return false;

	}
	
	public boolean mkdir(String name){
		try{
			realFile(absPath(name)).mkdir();
			return true;
		} catch(SmbException e){
		} catch (MalformedURLException e) {
		} catch (UnknownHostException e) {
		}
		return false;

	}
	
	public boolean create(String name) throws IOException{
		try{
			realFile(absPath(name)).createNewFile();
			return true;
		} catch(SmbException e){
		} catch (MalformedURLException e) {
		} catch (UnknownHostException e) {
		}
		return false;
	}
	
	public void cdabs(String path){
		String newPath = reducePath(path);
		try {
			SmbFile f = realFile(newPath);
			if(f.exists() && f.isDirectory())
				intSetCwd(newPath);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (SmbException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}
	
	public void cd(String path){
		cdabs( absPath(path) );
	}
	
	public boolean isDirectory(String file){
		try{
			realFile(absPath(file)).isDirectory();
			return true;
		} catch(SmbException e){
		} catch (MalformedURLException e) {
		} catch (UnknownHostException e) {
		}
		return false;
	}
	
		
	public String[] getFiles(){
		try{
			SmbFile f = realFile(cwd);
			return f.list();
		}catch(SmbException e){
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
		
}
