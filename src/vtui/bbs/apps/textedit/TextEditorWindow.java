package vtui.bbs.apps.textedit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import fun.useless.curses.Curses;
import fun.useless.curses.application.Application;
import fun.useless.curses.ui.Dimension;
import fun.useless.curses.ui.Position;
import fun.useless.curses.ui.components.MessageBox;
import fun.useless.curses.ui.components.MultiLineEdit;
import fun.useless.curses.ui.components.Window;

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
