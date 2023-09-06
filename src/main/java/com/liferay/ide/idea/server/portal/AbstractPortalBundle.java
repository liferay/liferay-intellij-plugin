/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.server.portal;

import com.liferay.ide.idea.util.FileUtil;

import java.nio.file.Path;

/**
 * @author Simon Jiang
 */
public abstract class AbstractPortalBundle implements PortalBundle {

	public AbstractPortalBundle(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("path cannot be null");
		}

		bundlePath = path;

		liferayHome = FileUtil.pathAppend(bundlePath, "..");

		autoDeployPath = FileUtil.pathAppend(liferayHome, "deploy");

		modulesPath = FileUtil.pathAppend(liferayHome, "osgi");
	}

	@Override
	public Path getAppServerDir() {
		return bundlePath;
	}

	@Override
	public Path getLiferayHome() {
		return liferayHome;
	}

	protected Path autoDeployPath;
	protected Path bundlePath;
	protected Path liferayHome;
	protected Path modulesPath;

}