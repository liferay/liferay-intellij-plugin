/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.core;

import com.intellij.openapi.components.PersistentStateComponent;
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
		return project.getService(LiferayProjectTypeService.class);
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