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

package com.liferay.ide.idea.server.portal;

import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.PathsUtil;

import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Simon Jiang
 */
public abstract class AbstractPortalBundleFactory implements PortalBundleFactory {

	@Override
	public Path canCreateFromPath(Path location) {
		Path retval = null;

		if (detectBundleDir(location) && _detectLiferayHome(PathsUtil.append(location, ".."))) {
			retval = location;
		}
		else if (_detectLiferayHome(location)) {
			File[] directories = FileUtil.getDirectories(location.toFile());

			for (File directory : directories) {
				Path dirPath = Paths.get(directory.getAbsolutePath());

				if (detectBundleDir(dirPath)) {
					retval = dirPath;

					break;
				}
			}
		}

		return retval;
	}

	protected abstract boolean detectBundleDir(Path path);

	private boolean _detectLiferayHome(Path path) {
		if (FileUtil.notExists(path)) {
			return false;
		}

		Path osgiPath = Paths.get(path.toString(), "osgi");

		if (FileUtil.exist(osgiPath)) {
			return true;
		}

		return false;
	}

}