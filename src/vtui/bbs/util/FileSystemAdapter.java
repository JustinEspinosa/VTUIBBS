package vtui.bbs.util;

import java.io.IOException;
import java.util.Map;

import textmode.util.FileAdapter;

public interface FileSystemAdapter {

	public static final char PATHSEP = '/';

	public abstract String absPath(String file);

	public abstract FileAdapter getFile(String file);

	public abstract String cwd();

	public abstract Map<String, FileAdapter> getTree(String folder);

	public abstract boolean copyToCwd(String from) throws IOException;

	public abstract boolean delete(String name);

	public abstract boolean mkdir(String name);

	public abstract boolean create(String name) throws IOException;

	public abstract void cdabs(String path);

	public abstract void cd(String path);

	public abstract boolean isDirectory(String file);

	public abstract String[] getFiles();

}