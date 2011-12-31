package vtui.bbs.util;

import javax.activation.MimetypesFileTypeMap;

import java.net.URL;
import java.security.AccessControlException;

import textmode.curses.Curses;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.TableBox;
import textmode.curses.ui.data.TableLayout;
import textmode.curses.ui.data.TableModel;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;
import textmode.util.FileAdapter;

public class FileBrowser extends TableBox implements ActionListener {


	
	private static class FSListModel implements TableModel{
		private MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();
		private FileSystemAdapter adapter;
		private String[] files;
		
		private FSListModel(URL url) throws InstantiationException, IllegalAccessException{
			adapter = FileSystem.getAdapter(url);
			adapter.cdabs(url.getPath());
			files = adapter.getFiles();
		}
		
		private boolean isDir(String file){
			return adapter.isDirectory(file);
		}
		
		private String cwd(){
			return adapter.cwd();
		}
		
		private void cd(String file){
			adapter.cd(file);
			files = adapter.getFiles();
		}

		public FileSystemAdapter getFs() {
			return adapter;
		}

		public void reload(){
			files = adapter.getFiles();
		}

		public int getColumnCount() {
			return 3;
		}

		public int getLineCount() {
			return files.length;
		}

		private static String[] unitnames={"B","KB","MB","GB","TB"};
		private String formatByteSize(long size){
			
			StringBuilder builder = new StringBuilder();
			int unit=0;
			
			while(size>1024 && unit<unitnames.length){
				size=size/1024;
				++unit; 
			}
			
			builder.append(size);
			builder.append(' ');
			builder.append(unitnames[unit]);
			return builder.toString();
		}
		
		public Object getItemAt(int line, int column) {
			FileAdapter f = adapter.getFile(files[line]);
			boolean isDir = false;
			try{
				isDir = f.isDirectory();
			}catch(AccessControlException e){
			}
			
			switch(column){
			case 0: 
				return f;
			case 1:
				if( isDir )
					return "<directory>";
				else
					try{
					return formatByteSize(f.length());
					}catch(AccessControlException e){
					return "???";
					}
			case 2:
				if( !isDir )
					return mimeMap.getContentType(f.getName());
			default: 
				return "";
			}
		}
		
	}
	
	private static class FSTableLayout implements TableLayout{

		public int getColumnWidth(int columnIdx) {
			switch(columnIdx){
			case 0:  return 30;
			case 1:  return 20;
			case 2:  return 30;
			default: return 8;
			}
		}
		
	}


	public FileBrowser(URL url,Curses cs, Position p, Dimension d) throws InstantiationException, IllegalAccessException {
		super(new FSTableLayout(), new FSListModel(url), cs, p, d);
		addActionListener(this);
	}
	
	private FSListModel intGetModel(){
		return (FSListModel)getModel();
	}
	
	public String getCwd(){
		return intGetModel().cwd();
	}

	public void upOneDir(){
		intGetModel().cd("..");
		updateFromModel();
	}
	
	public void actionPerformed(ActionEvent e) {
		String file = selectedItem().toString();
		if(intGetModel().isDir(file)){
			intGetModel().cd(file);
			updateFromModel();
		}
		
	}

	public FileSystemAdapter getFs() {
		return intGetModel().getFs();
	}
	
	public void reload(){
		intGetModel().reload();
		updateFromModel();
	}

}
