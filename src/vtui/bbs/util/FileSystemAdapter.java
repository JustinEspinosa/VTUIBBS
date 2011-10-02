package vtui.bbs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class FileSystemAdapter {
	
	public static final char PATHSEP='/';
	
	private File fsRoot;
	private String cwd;
	private String root;	
	
		
	public FileSystemAdapter(){
		fsRoot = new File("files");
		
		cwd    = String.valueOf(PATHSEP);
		root   = String.valueOf(PATHSEP);
	}
	
	private File realFile(String path){
		return new File(fsRoot.getAbsolutePath()+path);
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

	public File getFile(String file){
		return realFile(reducePath(file));
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
	
	private void addFile(String dir,Map<String,File> tree){
		File rf = realFile(reducePath(dir));
		
		if(rf.isDirectory()){
			String[] files = rf.list();
			for(String f: files)
				addFile(gluePathElements(dir,f),tree);
		
		}else{ tree.put(dir,rf); }
			
	}
	
	public Map<String,File> getTree(String folder){
		Map<String,File> tree = new HashMap<String,File>();
		
		addFile(folder,tree);
		
		return tree;
	}
	
	public boolean copyToCwd(String from) throws IOException{
		File f1 = realFile(reducePath(from));
		File f2 = realFile(absPath(f1.getName()));
		
		if(f2.createNewFile()){
			InputStream  is = new FileInputStream(f1);
			OutputStream os = new FileOutputStream(f2);
			
			int len = 0;
			byte[] data = new byte[1024];
			do{
				len = is.read(data);
				if(len>0)
					os.write(data,0,len);
			}while(len > 0);
				
			return true;
		}
			
		return false;
	}
	
	public boolean delete(String name){
		return realFile(absPath(name)).delete();
	}
	
	public boolean mkdir(String name){
		return realFile(absPath(name)).mkdir();
	}
	
	public boolean create(String name) throws IOException{
		return realFile(absPath(name)).createNewFile();
	}
	
	public void cdabs(String path){
		String newPath = reducePath(path);
		File f = realFile(newPath);
		if(f.exists() && f.isDirectory())
			intSetCwd(newPath);
	}
	
	public void cd(String path){
		cdabs( absPath(path) );
	}
	
	public boolean isDirectory(String file){
		return realFile(absPath(file)).isDirectory();
	}
	
		
	public String[] getFiles(){
		File f = realFile(cwd);
		
		return f.list();
	}
		
}
