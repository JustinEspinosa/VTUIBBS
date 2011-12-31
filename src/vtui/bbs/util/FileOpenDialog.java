package vtui.bbs.util;


import textmode.curses.Curses;
import textmode.curses.application.Application;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.Button;
import textmode.curses.ui.components.MessageBox;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;
import textmode.util.FileAdapter;


public class FileOpenDialog extends AbstractFileDialog {

	private Button okButton;
	private Button cancelButton;
	private FileAdapter selected = null;
	
	
	public FileOpenDialog(String title, Application app, Curses cs, Position p) {
		super(title, app, cs, p);
		initComponents();
	}
	private void initComponents(){
		okButton = new Button("Ok",curses(),new Position(buttonsLine(),5),10);
		okButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				prepareSelected();
				setResult(MessageBox.Result.OK);
				close();
			}
		});
		intAddChild(okButton);
		
		cancelButton = new Button("Cancel",curses(),new Position(buttonsLine(),25),10);
		cancelButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setResult(MessageBox.Result.CANCEL);
				close();
			}
		});
		intAddChild(cancelButton);
	}
	
	private void prepareSelected(){
		String path = browser().selectedItem().toString();
		selected = browser().getFs().getFile(path);
	}
	
	@Override
	public FileAdapter getSelectedFile() {
		return selected;
	}
	
	public static FileOpenDialog trySelect(String title, Application app, Curses cs){
		FileOpenDialog fodg = new FileOpenDialog(title, app, cs, app.nextPosition());
		app.showWindow(fodg);
		
		try {
			fodg.modalWait();
		} catch (InterruptedException e) {
		}
		
		return fodg;
	}
}
