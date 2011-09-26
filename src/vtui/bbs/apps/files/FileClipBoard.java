package vtui.bbs.apps.files;

public class FileClipBoard {
	private String p = null;
	
	public void put(String abspath){
		p = abspath;
	}
	
	public boolean hasFile(){
		return (p!=null);
	}
	
	public String get(){
		return p;
	}
}
