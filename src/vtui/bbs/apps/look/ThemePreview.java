package vtui.bbs.apps.look;

import textmode.curses.Curses;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.Button;
import textmode.curses.ui.components.Component;
import textmode.curses.ui.components.LineEdit;
import textmode.curses.ui.components.ListBox;
import textmode.curses.ui.components.MenuItem;
import textmode.curses.ui.components.Panel;
import textmode.curses.ui.components.PopUp;
import textmode.curses.ui.data.ListModel;

public class ThemePreview extends Panel<Component> {

	public ThemePreview(Curses cs, Position p) {
		super(cs, p, new Dimension(9,23));
		setBorder(true);
		clear();
		addComponents();
	}
	
	private void addComponents(){

		LineEdit edit = new LineEdit(curses(),new Position(1,1),10);
		edit.setText("Edit");
		intAddChild(edit);
		
		intAddChild(new Button("Button",curses(),new Position(3,1),10));
		
		
		intAddChild(new ListBox(new ListModel() {
			private String[] items = {"Item1","Item2"};
			public int getItemCount() {
				return items.length;
			}
			public Object getItemAt(int index) {
				return items[index];
			}
		},curses(),new Position(1,12),new Dimension(3,10)));
		
		PopUp popup = new PopUp(curses(), 10, null);
		popup.setPosition(new Position(5,12));
		popup.addItem(new MenuItem("Menu1", curses()));
		popup.addItem(new MenuItem("Menu2", curses()));
		intAddChild(popup);
		popup.open();
	}
	
	@Override
	public boolean acceptsFocus() {
		return false;
	}
	

}
