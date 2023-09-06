/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.server.portal;

import com.liferay.ide.idea.util.FileUtil;

import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Simon Jiang
 */
public abstract class AbstractPortalBundleFactory implements PortalBundleFactory {

	@Override
	public Path findAppServerPath(Path location) {
		Path retval = null;

		if (detectAppServerPath(location) && _detectLiferayHome(FileUtil.pathAppend(location, ".."))) {
			retval = location;
		}
		else if (_detectLiferayHome(location)) {
			File[] directories = FileUtil.getDirectories(location.toFile());

			for (File directory : directories) {
				Path dirPath = Paths.get(directory.getAbsolutePath());

				if (detectAppServerPath(dirPath)) {
					retval = dirPath;

					break;
				}
			}
		}

		return retval;
	}

	protected abstract boolean detectAppServerPath(Path path);

	private boolean _detectLiferayHome(Path path) {
		if (FileUtil.notExists(path)) {
			return false;
		}

		Path osgiPath = Paths.get(path.toString(), "osgi");

		if (FileUtil.exists(osgiPath)) {
			return true;
		}

		return false;
	}

}