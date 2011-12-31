package vtui.bbs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import jcifs.smb.Kerb5Authenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

import textmode.util.FileAdapter;

class SmbFileAdapter implements FileAdapter {

	private SmbFile wrapped;
	private Kerb5Authenticator smbAuth;
	
	public SmbFileAdapter(SmbFile file, Kerb5Authenticator auth) {
		wrapped = file;
		smbAuth = auth;
	}

	public String getName() {
		return wrapped.getName();
	}

	public InputStream getInputStream() throws IOException {
		return wrapped.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return getOutputStream(false);
	}

	public OutputStream getOutputStream(boolean append) throws IOException {
		return new SmbFileOutputStream(wrapped,append);
	}

	public FileAdapter getChild(String name) {
		try{
			return new SmbFileAdapter(new SmbFile(new URL(wrapped.getURL(),name),smbAuth),smbAuth );
		}catch(MalformedURLException e){
			throw new RuntimeException(e);
		}
	}

	public long length() {
		try {
			return wrapped.length();
		} catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isDirectory() {
		try {
			return wrapped.isDirectory();
		} catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean exists() {
		try {
			return wrapped.exists();
		} catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
