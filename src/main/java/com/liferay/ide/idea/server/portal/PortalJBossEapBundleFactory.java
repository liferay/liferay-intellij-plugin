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
import com.liferay.ide.idea.util.PropertiesUtil;

import java.io.File;

import java.nio.file.Path;

import java.util.Properties;

/**
 * @author Seiphon Wang
 */
public class PortalJBossEapBundleFactory extends PortalJBossBundleFactory {

	public static String getEAPVersionNoSlotCheck(
		File location, String metaInfPath, String[] versionPrefixs, String releaseName) {

		Path rootPath = location.toPath();

		Path eapDir = FileUtil.pathAppend(rootPath, metaInfPath);

		if (FileUtil.exists(eapDir)) {
			Path manifest = FileUtil.pathAppend(eapDir, "MANIFEST.MF");

			String type = JavaUtil.getManifestProperty(manifest.toFile(), "JBoss-Product-Release-Name");
			String version = JavaUtil.getManifestProperty(manifest.toFile(), "JBoss-Product-Release-Version");

			boolean matchesName = type.contains(releaseName);

			for (String prefixVersion : versionPrefixs) {
				boolean matchesVersion = version.startsWith(prefixVersion);

				if (matchesName && matchesVersion) {
					return version;
				}
			}
		}

		return null;
	}

	@Override
	public PortalBundle create(Path location) {
		return new PortalJBossEapBundle(location);
	}

	@Override
	public String getType() {
		return "jboss_eap";
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
			String eapVersion = _getEAPVersion(
				path.toFile(), _EAP_DIR_META_INF, new String[] {"6.", "7."}, "eap", "EAP");

			if (eapVersion != null) {
				return true;
			}

			return super.detectAppServerPath(path);
		}

		return false;
	}

	private String _getEAPVersion(
		File location, String metaInfPath, String[] versionPrefix, String slot, String releaseName) {

		Path rootPath = location.toPath();

		Path productConf = FileUtil.pathAppend(rootPath, "bin/product.conf");

		if (FileUtil.exists(productConf)) {
			Properties p = PropertiesUtil.loadProperties(productConf.toFile());

			if (p != null) {
				String product = (String)p.get("slot");

				if (slot.equals(product)) {
					return getEAPVersionNoSlotCheck(location, metaInfPath, versionPrefix, releaseName);
				}
			}
		}

		return null;
	}

	private static final String _EAP_DIR_META_INF = "modules/system/layers/base/org/jboss/as/product/eap/dir/META-INF";

}