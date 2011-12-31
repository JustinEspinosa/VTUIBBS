package vtui.bbs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import textmode.util.FileAdapter;

class IOFileAdapter implements FileAdapter {

	private File wrapped;
	
	public IOFileAdapter(File file) {
		wrapped = file;
	}

	public String getName() {
		return wrapped.getName();
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(wrapped);
	}

	public OutputStream getOutputStream() throws IOException {
		return getOutputStream(false);
	}

	public OutputStream getOutputStream(boolean append) throws IOException {
		return new FileOutputStream(wrapped,append);
	}

	public FileAdapter getChild(String name) {
		return new IOFileAdapter(new File(wrapped, name));
	}

	public long length() {
		return wrapped.length();
	}

	public boolean isDirectory() {
		return wrapped.isDirectory();
	}

	public boolean exists() {
		return wrapped.exists();
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
