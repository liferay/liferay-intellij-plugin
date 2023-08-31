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
	protected boolean detectAppServerPath(Path path) {
		if (FileUtil.notExists(path)) {
			return false;
		}

		Path binPath = FileUtil.pathAppend(path, "bin");
		Path confPath = FileUtil.pathAppend(path, "conf");
		Path libPath = FileUtil.pathAppend(path, "lib");
		Path webappPath = FileUtil.pathAppend(path, "webapps");

		if (FileUtil.exists(binPath) && FileUtil.exists(confPath) && FileUtil.exists(libPath) &&
			FileUtil.exists(webappPath)) {

			return true;
		}

		return false;
	}

}