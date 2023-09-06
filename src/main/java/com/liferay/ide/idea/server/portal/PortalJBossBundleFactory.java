/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.server.portal;

import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.JavaUtil;

import java.io.File;

import java.nio.file.Path;

/**
 * @author Seiphon Wang
 */
public class PortalJBossBundleFactory extends AbstractPortalBundleFactory {

	@Override
	public PortalBundle create(Path location) {
		return new PortalJBossBundle(location);
	}

	@Override
	public String getType() {
		return "jboss";
	}

	@Override
	protected boolean detectAppServerPath(Path path) {
		if (FileUtil.notExists(path)) {
			return false;
		}

		Path bundlesPath = FileUtil.pathAppend(path, "bundles");
		Path modulesPath = FileUtil.pathAppend(path, "modules");
		Path standalonePath = FileUtil.pathAppend(path, "standalone");
		Path binPath = FileUtil.pathAppend(path, "bin");

		if (FileUtil.exists(bundlesPath) && FileUtil.exists(modulesPath) && FileUtil.exists(standalonePath) &&
			FileUtil.exists(binPath)) {

			File mainFolder = new File("modules/org/jboss/as/server/main");

			Path mainFolderPath = mainFolder.toPath();

			return JavaUtil.scanFolderJarsForManifestProp(
				path.toFile(), mainFolderPath.toString(), _JBAS7_RELEASE_VERSION, "7.");
		}

		return false;
	}

	private static final String _JBAS7_RELEASE_VERSION = "JBossAS-Release-Version";

}