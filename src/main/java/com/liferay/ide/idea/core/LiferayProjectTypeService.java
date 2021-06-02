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

package com.liferay.ide.idea.core;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectType;

import com.liferay.ide.idea.ui.modules.LiferayProjectType;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Simon Jiang
 * @author Gregory Amerson
 */
@State(name = "ProjectType")
public class LiferayProjectTypeService implements PersistentStateComponent<ProjectType> {

	public static LiferayProjectTypeService getInstance(@NotNull Project project) {
		return ServiceManager.getService(project, LiferayProjectTypeService.class);
	}

	@NotNull
	public static ProjectType getProjectType(@NotNull Project project) {
		LiferayProjectTypeService instance = getInstance(project);

		ProjectType projectType = instance._projectType;

		if (projectType != null) {
			return projectType;
		}

		if (LiferayWorkspaceSupport.isValidGradleWorkspaceLocation(project.getBasePath())) {
			return new ProjectType(LiferayProjectType.LIFERAY_GRADLE_WORKSPACE);
		}

		if (LiferayWorkspaceSupport.isValidMavenWorkspaceProject(project)) {
			return new ProjectType(LiferayProjectType.LIFERAY_MAVEN_WORKSPACE);
		}

		throw new RuntimeException(MessagesBundle.message("invalid.workspace.location", project.getBasePath()));
	}

	public static void setProjectType(@NotNull Project project, @NotNull ProjectType projectType) {
		LiferayProjectTypeService instance = getInstance(project);

		instance.loadState(projectType);
	}

	@Nullable
	@Override
	public ProjectType getState() {
		return _projectType;
	}

	@Override
	public void loadState(@NotNull ProjectType projectType) {
		_projectType = projectType;
	}

	private ProjectType _projectType;

}