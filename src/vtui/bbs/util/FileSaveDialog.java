package vtui.bbs.util;

import textmode.curses.Curses;
import textmode.curses.application.Application;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.Button;
import textmode.curses.ui.components.LineEdit;
import textmode.curses.ui.components.MessageBox;
import textmode.curses.ui.event.ActionEvent;
import textmode.curses.ui.event.ActionListener;
import textmode.util.FileAdapter;


public class FileSaveDialog extends AbstractFileDialog {

	private Button okButton;
	private Button cancelButton;
	private LineEdit fName;
	private FileAdapter selected = null;
	
	public FileSaveDialog(String title, Application app, Curses cs, Position p) {
		super(title, app, cs, p);
		initComponents();
	}
	
	private void initComponents(){
		
		fName = new LineEdit(curses(), new Position(buttonsLine()-1,1), 38);
		intAddChild(fName);

		
		okButton = new Button("Ok",curses(),new Position(buttonsLine(),18),10);
		okButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(prepareSelected()){
					setResult(MessageBox.Result.OK);
					close();
				}
			}
		});
		intAddChild(okButton);
		
		cancelButton = new Button("Cancel",curses(),new Position(buttonsLine(),29),10);
		cancelButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setResult(MessageBox.Result.CANCEL);
				close();
			}
		});
		intAddChild(cancelButton);
		
	}

	private boolean prepareSelected(){
		boolean response = true;
		selected = browser().getFs().getFile(fName.getText());
		if(selected.exists())
			response = MessageBox.confirm("The file already exists.", "Overwrite?", getOwner(), curses());
		
		if(!response)
			selected = null;
		
		return response;
	}
	
	@Override
	public FileAdapter getSelectedFile() {
		return selected;
	}

	public static FileSaveDialog queryFileName(String title, Application app, Curses cs){
		FileSaveDialog fsdg = new FileSaveDialog(title, app, cs, app.nextPosition());
		app.showWindow(fsdg);
		
		try {
			fsdg.modalWait();
		} catch (InterruptedException e) {
		}
		
		return fsdg;
	}
}
