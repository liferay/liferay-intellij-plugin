/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.server.portal;

import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.JavaUtil;
import com.liferay.ide.idea.util.ListUtil;

import java.io.File;

import java.nio.file.Path;

import org.osgi.framework.Version;

/**
 * @author Simon Jiang
 */
public class PortalWildFlyBundleFactory extends PortalJBossBundleFactory {

	@Override
	public PortalBundle create(Path location) {
		return new PortalWildFlyBundle(location);
	}

	@Override
	public String getType() {
		return "wildfly";
	}

	@Override
	protected boolean detectAppServerPath(Path path) {
		if (FileUtil.notExists(path)) {
			return false;
		}

		Path modulesPath = FileUtil.pathAppend(path, "modules");
		Path standalonePath = FileUtil.pathAppend(path, "standalone");
		Path binPath = FileUtil.pathAppend(path, "bin");

		if (FileUtil.exists(modulesPath) && FileUtil.exists(standalonePath) && FileUtil.exists(binPath)) {
			String versions = getManifestPropFromJBossModulesFolder(
				new File[] {new File(path.toString(), "modules")}, "org.jboss.as.product",
				new String[] {"wildfly-full/dir/META-INF", "main/dir/META-INF"}, JBOSS_RELEASE_VERSION);

			if (versions != null) {
				Version version = Version.parseVersion(versions);

				if (version.compareTo(new Version("10.0")) >= 0) {
					return true;
				}

				return detectAppServerPath(path);
			}
		}

		return false;
	}

	protected String getManifestPropFromJBossModulesFolder(
		File[] moduleRoots, String moduleId, String[] slots, String property) {

		File[] layeredRoots = LayeredModulePathFactory.resolveLayeredModulePath(moduleRoots);

		for (File layeredFile : layeredRoots) {
			File[] manifests = LayeredModulePathFactory.getFilesForModule(
				layeredFile, moduleId, slots, (dir, name) -> name.equalsIgnoreCase("manifest.mf"));

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