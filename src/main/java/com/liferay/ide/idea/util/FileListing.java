/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Greg Amerson
 * @author Terry Jia
 */
public class FileListing {

	public static List<File> getFileListing(File aStartingDir) throws FileNotFoundException {
		List<File> result = new ArrayList<>();

		File[] filesAndDirs = aStartingDir.listFiles();

		List<File> filesDirs = Arrays.asList(filesAndDirs);

		for (File file : filesDirs) {
			result.add(file);

			if (!file.isFile()) {
				List<File> deeperList = getFileListing(file);

				result.addAll(deeperList);
			}
		}

		return result;
	}

}