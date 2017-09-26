package edu.ImportDLL;

import com.sun.jna.Library;
import com.sun.jna.Native;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImportDll {

	MyCode lib;

	public interface MyCode extends Library {
		void Print();
		void sayHello();
	};

	public void loadLibraryFromJar(String name) throws IOException {

		String path = "/native/";
		if (System.getProperty("os.arch").toLowerCase().endsWith("64")) {
			path += "64/";
		} else {
			throw new IllegalArgumentException("Not support this arch");
		}
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			path += "windows/" + name + ".dll";
		} else {
			throw new IllegalArgumentException("Not support this OS");
		}

		String[] parts = path.split("/");
		String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

		String prefix = "";
		String suffix = null;
		if (filename != null) {
			parts = filename.split("\\.", 2);
			prefix = parts[0];
			suffix = (parts.length > 1) ? "." + parts[parts.length - 1] : null; 
		}

		if (filename == null || prefix.length() < 3) {
			throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
		}

		File temp = File.createTempFile(prefix, suffix);
		temp.deleteOnExit();

		if (!temp.exists()) {
			throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
		}

		byte[] buffer = new byte[1024];
		int readBytes;

		InputStream is = ImportDll.class.getResourceAsStream(path);
		if (is == null) {
			throw new FileNotFoundException("File " + path + " was not found inside JAR.");
		}

		OutputStream os = new FileOutputStream(temp);
		try {
			while ((readBytes = is.read(buffer)) != -1) {
				os.write(buffer, 0, readBytes);
			}
		} finally {
			os.close();
			is.close();
		}

		this.lib = (MyCode) Native.loadLibrary(temp.getAbsolutePath(), MyCode.class);
	}

	public static void main(String[] args) throws IOException {

		ImportDll my = new ImportDll();

		my.loadLibraryFromJar("FunLib");

		my.lib.sayHello();
		my.lib.Print();
	}
}