package vtui.bbs.apps.textedit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import textmode.curses.Curses;
import textmode.curses.application.Application;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.MessageBox;
import textmode.curses.ui.components.MultiLineEdit;
import textmode.curses.ui.components.Window;


public class TextEditorWindow extends Window {

	private MultiLineEdit textContent;
	private File currentFile = null;
	
	public TextEditorWindow(String title, Application app,Curses cs, Position position, Dimension dimension) {
		super(title, app,cs, position, dimension);
		
		Dimension d = getSize().vertical(-1);
		textContent = new MultiLineEdit(cs,new Position(1, 0),d);	
		addChild(textContent);
		
	}
	
	public void load(File f){
		try {
			BufferedReader r = new BufferedReader(new FileReader(f));
			String l;
			
			if((l = r.readLine())!=null)
				textContent.appendText(l);
				
			while((l = r.readLine())!=null)
				textContent.appendLine(l);

			currentFile = f;
			
			r.close();
		} catch (FileNotFoundException e) {
			MessageBox.informUser("Error", "File not found.", getOwner(), curses());			
		} catch (IOException e) {
			MessageBox.informUser("Error", "Could not read everything.", getOwner(), curses());
		}
	}
	
	public boolean canSave(){
		return currentFile!=null;
	}
	
	public void saveAs(File f){
		currentFile = f;
		setTitle(currentFile.getName());
		save();
	}
	
	public void save(){
		
		if(currentFile ==null){
			MessageBox.informUser("Error", "Not bound to a file.", getOwner(), curses());
			return;
		}
			
		
		StringReader sr = new StringReader(textContent.getText());
		try {
			FileWriter fw = new FileWriter(currentFile);
			int c;
			while( (c=sr.read()) != -1)
				fw.write(c);
			
			fw.flush();
			fw.close();
		} catch (IOException e) {
			MessageBox.informUser("Error", "Could not write everything.", getOwner(), curses());
		}
	}
	
	@Override
	protected void userResized() {
		textContent.setSize(getSize().vertical(-1));
	}

}
