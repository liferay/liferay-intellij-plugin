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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class LiferayWorkspaceUtil {

	public static String getHomeDir(String location) {
		String result = _getGradleProperty(location, "liferay.workspace.home.dir", "bundles");

		if ((result == null) || result.equals("")) {
			return "bundles";
		}

		return result;
	}

	public static String getMavenProperty(Project project, String key, String defaultValue) {
		if (!isValidMavenWorkspaceLocation(project)) {
			return null;
		}

		MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);

		MavenProject mavenWorkspaceProject = mavenProjectsManager.findContainingProject(project.getBaseDir());

		Properties properties = mavenWorkspaceProject.getProperties();

		return properties.getProperty(key, defaultValue);
	}

	@NotNull
	public static String getModuleExtDir(Project project) {
		String retval = null;

		if (project != null) {
			String projectLocation = project.getBasePath();

			if (projectLocation != null) {
				retval = _getGradleProperty(
					projectLocation, WorkspaceConstants.DEFAULT_EXT_DIR_PROPERTY, WorkspaceConstants.DEFAULT_EXT_DIR);
			}
		}

		if (CoreUtil.isNullOrEmpty(retval)) {
			return WorkspaceConstants.DEFAULT_EXT_DIR;
		}

		return retval;
	}

	@Nullable
	public static VirtualFile getModuleExtDirFile(Project project) {
		String moduleExtDir = getModuleExtDir(project);

		File file = new File(moduleExtDir);

		if (!file.isAbsolute()) {
			file = new File(getWorkspaceLocation(project), moduleExtDir);
		}

		LocalFileSystem fileSystem = LocalFileSystem.getInstance();

		return fileSystem.findFileByPath(file.getPath());
	}

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

	@Nullable
	public static String getTargetPlatformVersion(Project project) {
		String location = project.getBasePath();

		return _getGradleProperty(location, WorkspaceConstants.DEFAULT_TARGET_PLATFORM_VERSION_PROPERTY, null);
	}

	public static File getWorkspaceLocation(Project project) {
		VirtualFile baseDir = project.getBaseDir();

		return new File(baseDir.getPath());
	}

	public static String getWorkspaceLocationPath(Project project) {
		return getWorkspaceLocation(project).getPath();
	}

	public static boolean isValidGradleWorkspaceLocation(String location) {
		File workspaceDir = new File(location);

		File buildGradle = new File(workspaceDir, _BUILD_GRADLE_FILE_NAME);
		File settingsGradle = new File(workspaceDir, _SETTINGS_GRADLE_FILE_NAME);
		File gradleProperties = new File(workspaceDir, _GRADLE_PROPERTIES_FILE_NAME);

		if (!(buildGradle.exists() && settingsGradle.exists() && gradleProperties.exists())) {
			return false;
		}

		String settingsContent = FileUtil.readContents(settingsGradle, true);

		Matcher matcher = _patternWorkspacePlugin.matcher(settingsContent);

		if ((settingsContent != null) && matcher.matches()) {
			return true;
		}

		return false;
	}

	public static boolean isValidMavenWorkspaceLocation(Project project) {
		try {
			MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);

			if (!mavenProjectsManager.isMavenizedProject()) {
				return false;
			}

			MavenProject mavenWorkspaceProject = mavenProjectsManager.findContainingProject(project.getBaseDir());

			if (mavenWorkspaceProject == null) {
				return false;
			}

			MavenPlugin liferayWorkspacePlugin = mavenWorkspaceProject.findPlugin(
				"com.liferay", "com.liferay.portal.tools.bundle.support");

			if (liferayWorkspacePlugin != null) {
				return true;
			}
		}
		catch (Exception e) {
			return false;
		}

		return false;
	}

	public static boolean isValidWorkspaceLocation(Project project) {
		if ((project != null) &&
			(isValidGradleWorkspaceLocation(project.getBasePath()) || isValidMavenWorkspaceLocation(project))) {

			return true;
		}

		return false;
	}

	private static String _getGradleProperty(String projectLocation, String key, String defaultValue) {
		File gradleProperties = new File(projectLocation, "gradle.properties");

		if (gradleProperties.exists()) {
			Properties properties = PropertiesUtil.loadProperties(gradleProperties);

			return properties.getProperty(key, defaultValue);
		}

		return "";
	}

	private static final String _BUILD_GRADLE_FILE_NAME = "build.gradle";

	private static final String _GRADLE_PROPERTIES_FILE_NAME = "gradle.properties";

	private static final String _SETTINGS_GRADLE_FILE_NAME = "settings.gradle";

	private static final Pattern _patternWorkspacePlugin = Pattern.compile(
		".*apply.*plugin.*:.*[\'\"]com\\.liferay\\.workspace[\'\"].*", Pattern.MULTILINE | Pattern.DOTALL);

}