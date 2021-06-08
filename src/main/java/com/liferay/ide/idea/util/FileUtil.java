/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.idea.util;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.URL;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class FileUtil {

	public static void copyFile(File src, File dest) {
		if ((src == null) || !src.exists() || (dest == null) || dest.isDirectory()) {
			return;
		}

		byte[] buf = new byte[4096];

		try (FileInputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
			int avail = in.read(buf);

			while (avail > 0) {
				out.write(buf, 0, avail);
				avail = in.read(buf);
			}
		}
		catch (Exception exception) {
		}
	}

	public static void copyFileToDir(File src, File dir) {
		copyFileToDir(src, src.getName(), dir);
	}

	public static void copyFileToDir(File src, String newName, File dir) {
		copyFile(src, new File(dir, newName));
	}

	public static boolean exists(File file) {
		if ((file != null) && file.exists()) {
			return true;
		}

		return false;
	}

	public static boolean exists(Path path) {
		if ((path != null) && exists(path.toFile())) {
			return true;
		}

		return false;
	}

	public static boolean exists(VirtualFile file) {
		if ((file != null) && file.exists()) {
			return true;
		}

		return false;
	}

	public static File[] getDirectories(File directory) {
		return directory.listFiles(file -> file.isDirectory());
	}

	public static File getFile(URL url) {
		if (url == null) {
			return null;
		}

		return new File(url.getFile());
	}

	public static Path getPath(String location) {
		Path newPath = Paths.get(location);

		return newPath.toAbsolutePath();
	}

	public static boolean notExists(File file) {
		if ((file == null) || !file.exists()) {
			return true;
		}

		return false;
	}

	public static boolean notExists(Path path) {
		if (path == null) {
			return true;
		}

		if (notExists(path.toFile())) {
			return true;
		}

		return false;
	}

	public static boolean notExists(VirtualFile virtualFile) {
		if ((virtualFile == null) || !virtualFile.exists()) {
			return true;
		}

		return false;
	}

	public static Path pathAppend(Path path, String child) {
		Path newPath = Paths.get(path.toString(), child);

		return newPath.toAbsolutePath();
	}

	public static Path pathAppend(String path, String child) {
		Path newPath = Paths.get(path, child);

		return newPath.toAbsolutePath();
	}

	public static String readContents(File file, boolean includeNewlines) {
		if (file == null) {
			return null;
		}

		if (!file.exists()) {
			return null;
		}

		StringBuffer contents = new StringBuffer();
		BufferedReader bufferedReader = null;

		try {
			FileReader fileReader = new FileReader(file);

			bufferedReader = new BufferedReader(fileReader);

			String line;

			while ((line = bufferedReader.readLine()) != null) {
				contents.append(line);

				if (includeNewlines) {
					contents.append(System.getProperty("line.separator"));
				}
			}
		}
		catch (Exception exception) {
		}
		finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				}
				catch (IOException ioException) {
				}
			}
		}

		return contents.toString();
	}

	public static void writeFile(File f, byte[] contents, String expectedProjectName) {
		writeFile(f, new ByteArrayInputStream(contents), expectedProjectName);
	}

	public static void writeFile(File f, InputStream contents) {
		writeFile(f, contents, null);
	}

	public static void writeFile(File f, InputStream contents, String expectedProjectName) {
		if (f.exists()) {
			if (f.isDirectory()) {
			}
		}
		else {
			File parentFile = f.getParentFile();

			parentFile.mkdirs();
		}

		if (f.exists() && !f.canWrite()) {
			return;
		}

		byte[] buffer = new byte[1024];

		try (FileOutputStream out = new FileOutputStream(f)) {
			for (int count; (count = contents.read(buffer)) != -1;) {
				out.write(buffer, 0, count);
			}

			out.flush();
		}
		catch (IOException ioException) {
		}
	}

	public static void writeFile(File f, String contents, String expectedProjectName) {
		try {
			writeFile(f, contents.getBytes("UTF-8"), expectedProjectName);
		}
		catch (UnsupportedEncodingException unsupportedEncodingException) {
			throw new RuntimeException(unsupportedEncodingException);
		}
	}

}