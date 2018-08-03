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
import com.liferay.ide.idea.util.JavaUtil;
import com.liferay.ide.idea.util.ListUtil;

import java.io.File;

import java.nio.file.Path;

/**
 * @author Simon Jiang
 */
public class PortalWildFlyBundleFactory extends AbstractPortalBundleFactory {

	@Override
	public PortalBundle create(Path location) {
		return new PortalWildFlyBundle(location);
	}

	@Override
	public String getType() {
		return "wildfly";
	}

	@Override
	protected boolean detectBundleDir(Path path) {
		if (FileUtil.notExists(path)) {
			return false;
		}

		Path modulesPath = FileUtil.pathAppend(path, "modules");
		Path standalonePath = FileUtil.pathAppend(path, "standalone");
		Path binPath = FileUtil.pathAppend(path, "bin");

		if (FileUtil.exist(modulesPath) && FileUtil.exist(standalonePath) && FileUtil.exist(binPath)) {
			String versions = getManifestPropFromJBossModulesFolder(
				new File[] {new File(path.toString(), "modules")}, "org.jboss.as.product", "wildfly-full/dir/META-INF",
				JBOSS_RELEASE_VERSION);

			if ((versions != null) && (versions.startsWith("10.") || versions.startsWith("11."))) {
				return true;
			}
			else {
				return false;
			}
		}

		return false;
	}

	protected String getManifestPropFromJBossModulesFolder(
		File[] moduleRoots, String moduleId, String slot, String property) {

		File[] layeredRoots = LayeredModulePathFactory.resolveLayeredModulePath(moduleRoots);

		for (File layeredFile : layeredRoots) {
			File[] manifests = LayeredModulePathFactory.getFilesForModule(
				layeredFile, moduleId, slot, (dir, name) -> name.equalsIgnoreCase("manifest.mf"));

			if (ListUtil.isNotEmpty(manifests)) {
				String value = JavaUtil.getManifestProperty(manifests[0], property);

				if (value != null) {
					return value;
				}

				return null;
			}
		}

		return null;
	}

	protected static final String JBOSS_RELEASE_VERSION = "JBoss-Product-Release-Version";

}