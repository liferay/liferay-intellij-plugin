/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.core;

import com.intellij.openapi.project.Project;

import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.MavenUtil;

import java.util.Objects;
import java.util.Properties;

import org.jetbrains.idea.maven.project.MavenProject;

/**
 * @author Simon Jiang
 */
public class LiferayMavenWorkspaceProvider extends AbstractWorkspaceProvider {

	public LiferayMavenWorkspaceProvider() {
	}

	public LiferayMavenWorkspaceProvider(Project project) {
		super(project);
	}

	@Override
	public String getLiferayHome() {
		MavenProject mavenWorkspaceProject = MavenUtil.getWorkspaceMavenProject(project);

		Properties properties = mavenWorkspaceProject.getProperties();

		return properties.getProperty(WorkspaceConstants.MAVEN_HOME_DIR_PROPERTY, WorkspaceConstants.HOME_DIR_DEFAULT);
	}

	@Override
	public String getTargetPlatformVersion() {
		return getWorkspaceProperty(WorkspaceConstants.WORKSPACE_BOM_VERSION, null);
	}

	@Override
	public String[] getWorkspaceModuleDirs() {
		String workspaceBomVersion = getTargetPlatformVersion();

		if (Objects.nonNull(workspaceBomVersion)) {
			return null;
		}

		return new String[] {"modules"};
	}

	@Override
	public String getWorkspaceProperty(String key, String defaultValue) {
		MavenProject mavenWorkspaceProject = MavenUtil.getWorkspaceMavenProject(project);

		Properties properties = mavenWorkspaceProject.getProperties();

		return properties.getProperty(key, defaultValue);
	}

	@Override
	public String[] getWorkspaceWarDirs() {
		String workspaceBomVersion = getTargetPlatformVersion();

		if (Objects.nonNull(workspaceBomVersion)) {
			return null;
		}

		return new String[] {"wars"};
	}

	@Override
	public boolean isFlexibleLiferayWorkspace() {
		MavenProject mavenWorkspaceProject = MavenUtil.getWorkspaceMavenProject(project);

		Properties properties = mavenWorkspaceProject.getProperties();

		return Objects.nonNull(properties.getProperty(WorkspaceConstants.WORKSPACE_BOM_VERSION, null));
	}

	@Override
	public <T> T provide(Project project, Class<T> adapterType) {
		if (LiferayWorkspaceSupport.isValidMavenWorkspaceProject(project)) {
			return adapterType.cast(new LiferayMavenWorkspaceProvider(project));
		}

		return null;
	}

}