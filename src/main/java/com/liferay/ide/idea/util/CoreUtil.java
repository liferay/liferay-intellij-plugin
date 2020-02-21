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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.osgi.framework.Version;

/**
 * @author Terry Jia
 */
public class CoreUtil {

	public static int compareVersions(Version v1, Version v2) {
		if (v2 == v1) {

			// quicktest

			return 0;
		}

		int result = v1.getMajor() - v2.getMajor();

		if (result != 0) {
			return result;
		}

		result = v1.getMinor() - v2.getMinor();

		if (result != 0) {
			return result;
		}

		result = v1.getMicro() - v2.getMicro();

		if (result != 0) {
			return result;
		}

		String s1 = v1.getQualifier();

		return s1.compareTo(v2.getQualifier());
	}

	public static boolean isNullOrEmpty(Object[] array) {
		if ((array == null) || (array.length == 0)) {
			return true;
		}

		return false;
	}

	public static boolean isNullOrEmpty(String val) {
		if (val == null) {
			return true;
		}

		String trimmedVal = val.trim();

		if (val.equals(StringPool.EMPTY) || trimmedVal.equals(StringPool.EMPTY)) {
			return true;
		}

		return false;
	}

	public static String readStreamToString(InputStream contents) throws IOException {
		return readStreamToString(contents, true);
	}

	public static String readStreamToString(InputStream contents, boolean closeStream) throws IOException {
		if (contents == null) {
			return null;
		}

		char[] buffer = new char[0x10000];

		StringBuilder out = new StringBuilder();

		try (Reader in = new InputStreamReader(contents, "UTF-8")) {
			int read;

			do {
				read = in.read(buffer, 0, buffer.length);

				if (read > 0) {
					out.append(buffer, 0, read);
				}
			}
			while (read >= 0);
		}

		if (closeStream) {
			contents.close();
		}

		return out.toString();
	}

}