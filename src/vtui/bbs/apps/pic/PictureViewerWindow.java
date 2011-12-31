package vtui.bbs.apps.pic;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import textmode.curses.Curses;
import textmode.curses.application.Application;
import textmode.curses.ui.ColorTable;
import textmode.curses.ui.Dimension;
import textmode.curses.ui.Position;
import textmode.curses.ui.components.PictureBox;
import textmode.curses.ui.components.ScrollPane;
import textmode.curses.ui.components.Window;
import textmode.curses.ui.event.CharacterCodeEvent;
import textmode.curses.ui.event.UiEvent;
import textmode.curses.ui.event.UiInputEvent;
import textmode.graphics.core.ASCIIPicture;
import textmode.graphics.core.Bitmap;
import textmode.graphics.core.Resolution;
import textmode.util.FileAdapter;


public class PictureViewerWindow extends Window {
	private class Scroller extends ScrollPane<PictureBox>{
		public Scroller() {
			super(picBox,PictureViewerWindow.this.curses(), new Position(1,0), PictureViewerWindow.this.getSize().vertical(-1));
		}
		protected void redraw() {
			picBox.refresh();
		}
	};
	
	private Scroller scroller;
	private PictureBox picBox;
	private Bitmap bitmap;
	private Resolution resolution = new Resolution(6.0);
	
	public PictureViewerWindow(FileAdapter picture, Application app,Curses cs, Position position, Dimension dimension) throws IOException {
		super(picture.getName(), app,cs, position, dimension);
		loadImage(picture);
		picBox = new PictureBox(generateAscii(),cs,Position.ORIGIN);
		scroller = new Scroller();
		addChild(scroller);
		
	}
	
	private void loadImage(FileAdapter file) throws IOException{
		BufferedImage img = ImageIO.read(file.getInputStream());
		bitmap = new Bitmap(img);
	}
	
	private ASCIIPicture generateAscii(){
		return bitmap.ASCIIDither(resolution, ColorTable.XTermColor256);
	}
	
	public void setPicture(ASCIIPicture pic){
		picBox.setPicture(pic);
	}
	
	@Override
	protected void userResized() {
		scroller.setSize(getSize().vertical(-1));
	}
	
	public void setResolution(Resolution res) {
		resolution = res;
		setPicture(generateAscii());
	}

	@Override
	public void processEvent(UiEvent e) {
		if(e instanceof UiInputEvent && ((UiInputEvent)e).getOriginalEvent() instanceof CharacterCodeEvent){
			CharacterCodeEvent cce = (CharacterCodeEvent)((UiInputEvent)e).getOriginalEvent();
			
			if(cce.getChar()=='+'){
				setResolution(resolution.scale(-1)); return; }
			
			if(cce.getChar()=='-'){
				setResolution(resolution.scale(1));  return; }
		}
		super.processEvent(e);
	}
}
