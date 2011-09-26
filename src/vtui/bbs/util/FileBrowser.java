package vtui.bbs.util;

import fun.useless.curses.Curses;
import fun.useless.curses.ui.Dimension;
import fun.useless.curses.ui.Position;
import fun.useless.curses.ui.components.ListBox;
import fun.useless.curses.ui.data.ListBoxModel;
import fun.useless.curses.ui.event.ActionEvent;
import fun.useless.curses.ui.event.ActionListener;

public class FileBrowser extends ListBox implements ActionListener {

	private static class FSListModel implements ListBoxModel{

		private FileSystemAdapter adapter;
		private String[] files;
		
		private FSListModel(String path){
			adapter = new FileSystemAdapter();
			adapter.cdabs(path);
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
		
		public int getItemCount() {
			return files.length;
		}

		public Object getItemAt(int index) {
			String f= files[index];
			if(adapter.isDirectory(f))
				f+=FileSystemAdapter.PATHSEP;
			return f;
		}

		public FileSystemAdapter getFs() {
			return adapter;
		}
		
		public void reload(){
			files = adapter.getFiles();
		}
		
	}


	public FileBrowser(String path,Curses cs, Position p, Dimension d) {
		super(new FSListModel(path), cs, p, d);
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
