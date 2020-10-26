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
		String targetPlatformVersion = getWorkspaceProperty(WorkspaceConstants.WORKSPACE_BOM_VERSION, null);

		if (Objects.nonNull(targetPlatformVersion) && targetPlatformVersion.contains("-")) {
			targetPlatformVersion = targetPlatformVersion.substring(0, targetPlatformVersion.indexOf("-"));
		}

		return targetPlatformVersion;
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
		if (LiferayWorkspaceSupport.isValidMavenWorkspaceLocation(project)) {
			return adapterType.cast(new LiferayMavenWorkspaceProvider(project));
		}

		return null;
	}

}