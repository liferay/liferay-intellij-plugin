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
public class PortalTomcatBundleFactory extends AbstractPortalBundleFactory {

	@Override
	public PortalBundle create(Path location) {
		return new PortalTomcatBundle(location);
	}

	@Override
	public String getType() {
		return "tomcat";
	}

	@Override
	protected boolean detectBundleDir(Path path) {
		if (FileUtil.notExists(path)) {
			return false;
		}

		Path binPath = FileUtil.pathAppend(path, "bin");
		Path confPath = FileUtil.pathAppend(path, "conf");
		Path libPath = FileUtil.pathAppend(path, "lib");
		Path webappPath = FileUtil.pathAppend(path, "webapps");

		if (FileUtil.exist(binPath) && FileUtil.exist(confPath) && FileUtil.exist(libPath) &&
			FileUtil.exist(webappPath)) {

			return true;
		}

		return false;
	}

}