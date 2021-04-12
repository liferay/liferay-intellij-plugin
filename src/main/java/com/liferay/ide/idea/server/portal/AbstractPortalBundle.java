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