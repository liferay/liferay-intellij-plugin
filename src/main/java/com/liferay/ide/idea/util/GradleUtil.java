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

import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.ExternalProjectInfo;
import com.intellij.openapi.externalSystem.model.ProjectKeys;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.plugins.gradle.settings.GradleExtensionsSettings;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Terry Jia
 */
public class GradleUtil {

	public static List<LibraryData> getTargetPlatformArtifacts(Project project) {
		ProjectDataManager manager = ProjectDataManager.getInstance();

		Collection<ExternalProjectInfo> projectsData = manager.getExternalProjectsData(
			project, GradleConstants.SYSTEM_ID);

		for (ExternalProjectInfo projectInfo : projectsData) {
			DataNode<ProjectData> projectDataNode = projectInfo.getExternalProjectStructure();

			if (projectDataNode == null) {
				continue;
			}

			Collection<DataNode<?>> nodes = projectDataNode.getChildren();

			List<LibraryData> libData = new ArrayList<>(nodes.size());

			for (DataNode child : nodes) {
				if (!ProjectKeys.LIBRARY.equals(child.getKey())) {
					continue;
				}

				libData.add((LibraryData)child.getData());
			}

			libData.sort(
				(o1, o2) -> {
					String artifactId = o1.getArtifactId();

					return artifactId.compareToIgnoreCase(o2.getArtifactId());
				});

			return libData;
		}

		return Collections.emptyList();
	}

	public static boolean isWatchableProject(Module module) {
		GradleExtensionsSettings.Settings settings = GradleExtensionsSettings.getInstance(module.getProject());

		GradleExtensionsSettings.GradleExtensionsData gradleExtensionsData = settings.getExtensionsFor(module);

		if (gradleExtensionsData == null) {
			return false;
		}

		List<GradleExtensionsSettings.GradleTask> gradleTasks = gradleExtensionsData.tasks;

		for (GradleExtensionsSettings.GradleTask gradleTask : gradleTasks) {
			if ("watch".equals(gradleTask.name) &&
				"com.liferay.gradle.plugins.tasks.WatchTask".equals(gradleTask.typeFqn)) {

				return true;
			}
		}

		return false;
	}

}