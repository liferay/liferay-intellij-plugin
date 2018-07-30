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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Simon Jiang
 */
public class PathsUtil {

	public static Path append(Path path, String child) {
		Path newPath = Paths.get(path.toString(), child);

		return newPath.toAbsolutePath();
	}

	public static Path append(String path, String child) {
		Path newPath = Paths.get(path, child);

		return newPath.toAbsolutePath();
	}

	public static Path getPath(String location) {
		Path newPath = Paths.get(location);

		return newPath.toAbsolutePath();
	}

}