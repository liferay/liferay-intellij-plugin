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

package com.liferay.ide.idea.util;

import com.intellij.openapi.util.Version;

import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 * @author Terry Jia
 */
public class GradleUtil {

	public static boolean isWatchableProject(File buildFile) {
		if (FileUtil.notExists(buildFile)) {
			return false;
		}

		boolean watchable = false;

		try {
			GradleDependencyUpdater updater = new GradleDependencyUpdater(buildFile);

			List<GradleDependency> dependencies = updater.getAllBuildDependencies();

			for (GradleDependency dependency : dependencies) {
				String group = dependency.getGroup();
				String name = dependency.getName();
				Version version = new Version(0, 0, 0);
				String dependencyVersion = dependency.getVersion();

				if ((dependencyVersion != null) && !dependencyVersion.equals("")) {
					version = Version.parseVersion(dependencyVersion);
				}

				if ("com.liferay".equals(group) && "com.liferay.gradle.plugins".equals(name) &&
					version.isOrGreaterThan(3, 11)) {

					watchable = true;

					break;
				}

				if ("com.liferay".equals(group) && "com.liferay.gradle.plugins.workspace".equals(name) &&
					version.isOrGreaterThan(1, 9, 2)) {

					watchable = true;

					break;
				}
			}
		}
		catch (IOException ioe) {
		}

		return watchable;
	}

}