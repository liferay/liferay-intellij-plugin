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

import com.intellij.openapi.project.Project;

import java.io.File;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

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