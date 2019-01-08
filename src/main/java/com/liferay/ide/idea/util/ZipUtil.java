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

import com.intellij.openapi.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Terry Jia
 */
public class ZipUtil {

	public static ZipFile open(File file) throws IOException {
		try {
			return new ZipFile(file);
		}
		catch (FileNotFoundException fnfe) {
			FileNotFoundException e = new FileNotFoundException(file.getAbsolutePath());

			e.initCause(fnfe);

			throw e;
		}
	}

	public static void unzip(File file, File destDir, PathFilter filter) throws IOException {
		ZipFile zip = open(file);

		try {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			Map<String, File> folders = new HashMap<>();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				String entryName = entry.getName();

				if (!folders.isEmpty()) {
					boolean hasCopied = false;

					for (Map.Entry<String, File> e : folders.entrySet()) {
						if (entryName.startsWith(e.getKey())) {
							//if the entry folder is accepted that means the sub-nodes should be accepted too

							_copyEntry(zip, entry, e.getValue());
							hasCopied = true;

							break;
						}
					}

					if (hasCopied) {
						continue;
					}
				}

				if (filter != null) {
					Pair<Boolean, File> pair = filter.accept(entryName);

					if (pair.getFirst()) {
						if (entry.isDirectory()) {
							folders.put(entryName, pair.getSecond());
						}

						_copyEntry(zip, entry, pair.getSecond());
					}
				}
				else {
					_copyEntry(zip, entry, destDir);
				}
			}
		}
		finally {
			try {
				zip.close();
			}
			catch (IOException ioe) {
			}
		}
	}

	@FunctionalInterface
	public interface PathFilter {

		/**
		 * A filter for zip entry
		 *
		 * @return a pair of return values, if the input entry path is accepted then
		 * return true and the expected directory, otherwise return false and null.
		 */
		public Pair<Boolean, File> accept(String entryPath);

	}

	private static void _copyEntry(ZipFile zip, ZipEntry entry, File destDir) throws IOException {
		String entryName = entry.getName();

		if (entry.isDirectory()) {
			File emptyDir = new File(destDir, entryName);

			_mkdir(emptyDir);

			return;
		}

		File f = new File(destDir, entryName);

		File dir = f.getParentFile();

		_mkdir(dir);

		try (InputStream in = zip.getInputStream(entry);
			FileOutputStream out = new FileOutputStream(f)) {

			byte[] bytes = new byte[1024];

			int count = in.read(bytes);

			while (count != -1) {
				out.write(bytes, 0, count);
				count = in.read(bytes);
			}

			out.flush();
		}
	}

	private static void _mkdir(File dir) throws IOException {
		if (!dir.exists() && !dir.mkdirs()) {
			String msg = "Could not create dir: " + dir.getPath();

			throw new IOException(msg);
		}
	}

}