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

import com.intellij.openapi.module.Module;

import java.util.List;

import org.jetbrains.plugins.gradle.settings.GradleExtensionsSettings;

/**
 * @author Terry Jia
 */
public class GradleUtil {

	public static boolean isWatchableProject(Module module) {
		GradleExtensionsSettings.Settings instance = GradleExtensionsSettings.getInstance(module.getProject());

		GradleExtensionsSettings.GradleExtensionsData extensions = instance.getExtensionsFor(module);

		if (extensions == null) {
			return false;
		}

		List<GradleExtensionsSettings.GradleTask> tasks = extensions.tasks;

		for (GradleExtensionsSettings.GradleTask task : tasks) {
			if ("watch".equals(task.name) && "com.liferay.gradle.plugins.tasks.WatchTask".equals(task.typeFqn)) {
				return true;
			}
		}

		return false;
	}

}