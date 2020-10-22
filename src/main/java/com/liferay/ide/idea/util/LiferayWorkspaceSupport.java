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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import com.intellij.openapi.externalSystem.service.notification.ExternalSystemNotificationManager;
import com.intellij.openapi.externalSystem.service.notification.NotificationCategory;
import com.intellij.openapi.externalSystem.service.notification.NotificationData;
import com.intellij.openapi.externalSystem.service.notification.NotificationSource;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayCore;
import com.liferay.ide.idea.core.ProductInfo;
import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.core.WorkspaceProvider;

import java.io.File;

import java.nio.file.Files;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Terry Jia
 * @author Simon Jiang
 * @author Ethan Sun
 */
public interface LiferayWorkspaceSupport {

	public static Map<String, ProductInfo> getProductInfos(Project project) {
		try (JsonReader jsonReader = new JsonReader(Files.newBufferedReader(_workspaceCacheFile.toPath()))) {
			Gson gson = new Gson();

			TypeToken<Map<String, ProductInfo>> typeToken = new TypeToken<Map<String, ProductInfo>>() {
			};

			return gson.fromJson(jsonReader, typeToken.getType());
		}
		catch (Exception e) {
			NotificationData notificationData = new NotificationData(
				"<b>Cannot Find Product Info</b>", "<i>" + project.getName() + "</i> \n" + e.getMessage(),
				NotificationCategory.WARNING, NotificationSource.TASK_EXECUTION);

			notificationData.setBalloonNotification(true);

			ExternalSystemNotificationManager externalSystemNotificationManager =
				ExternalSystemNotificationManager.getInstance(project);

			externalSystemNotificationManager.showNotification(GradleConstants.SYSTEM_ID, notificationData);
		}

		return null;
	}

	@Nullable
	public static VirtualFile getWorkspaceVirtualFile(@Nullable Project project) {
		if (project == null) {
			return null;
		}

		String projectBasePath = project.getBasePath();

		if (projectBasePath == null) {
			return null;
		}

		LocalFileSystem fileSystem = LocalFileSystem.getInstance();

		return fileSystem.findFileByPath(projectBasePath);
	}

	public static boolean isFlexibleLiferayWorkspace(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return false;
		}

		return workspaceProvider.isFlexibleLiferayWorkspace();
	}

	public static boolean isValidGradleWorkspaceLocation(@Nullable String location) {
		if (location == null) {
			return false;
		}

		File workspaceDir = new File(location);

		File buildGradle = new File(workspaceDir, BUILD_GRADLE_FILE_NAME);
		File settingsGradle = new File(workspaceDir, SETTINGS_GRADLE_FILE_NAME);
		File gradleProperties = new File(workspaceDir, GRADLE_PROPERTIES_FILE_NAME);

		if (!(buildGradle.exists() && settingsGradle.exists() && gradleProperties.exists())) {
			return false;
		}

		String settingsContent = FileUtil.readContents(settingsGradle, true);

		Matcher matcher = PATTERN_WORKSPACE_PLUGIN.matcher(settingsContent);

		return matcher.matches();
	}

	public static boolean isValidGradleWorkspaceProject(Project project) {
		return isValidGradleWorkspaceLocation(project.getBasePath());
	}

	public static boolean isValidMavenWorkspaceLocation(Project project) {
		if (project == null) {
			return false;
		}

		try {
			MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);

			if (!mavenProjectsManager.isMavenizedProject()) {
				return false;
			}

			VirtualFile workspaceVirtualFile = getWorkspaceVirtualFile(project);

			if (workspaceVirtualFile == null) {
				return false;
			}

			MavenProject mavenWorkspaceProject = mavenProjectsManager.findContainingProject(workspaceVirtualFile);

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

	public default String getHomeDir(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return null;
		}

		String result = workspaceProvider.getWorkspaceProperty(
			WorkspaceConstants.HOME_DIR_PROPERTY, WorkspaceConstants.HOME_DIR_DEFAULT);

		if (CoreUtil.isNullOrEmpty(result)) {
			return WorkspaceConstants.HOME_DIR_DEFAULT;
		}

		return result;
	}

	public default boolean getIndexSources(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return false;
		}

		return workspaceProvider.getIndexSources();
	}

	@Nullable
	public default String getLiferayVersion(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return null;
		}

		return workspaceProvider.getLiferayVersion();
	}

	@Nullable
	public default String getMavenProperty(Project project, String key, String defaultValue) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return null;
		}

		if (workspaceProvider.isGradleWorkspace()) {
			return null;
		}

		return workspaceProvider.getWorkspaceProperty(key, defaultValue);
	}

	@Nullable
	public default VirtualFile getModuleExtDirFile(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return null;
		}

		if (!workspaceProvider.isGradleWorkspace()) {
			return null;
		}

		return workspaceProvider.getModuleExtDirFile();
	}

	@Nullable
	public default String getTargetPlatformVersion(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return null;
		}

		return workspaceProvider.getTargetPlatformVersion();
	}

	public default String getWorkspaceModuleDir(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		String[] workspaceModuleDirs = workspaceProvider.getWorkspaceModuleDirs();

		if (!Objects.isNull(workspaceModuleDirs)) {
			return workspaceModuleDirs[0];
		}

		return null;
	}

	public default String getWorkspaceProperty(Project project, String key, String defaultValue) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return null;
		}

		return workspaceProvider.getWorkspaceProperty(key, defaultValue);
	}

	public final String BUILD_GRADLE_FILE_NAME = "build.gradle";

	public final String DEFAULT_WORKSPACE_CACHE_FILE = ".liferay/workspace/.product_info.json";

	public final String GRADLE_PROPERTIES_FILE_NAME = "gradle.properties";

	public final Pattern PATTERN_WORKSPACE_PLUGIN = Pattern.compile(
		".*apply.*plugin.*:.*[\'\"]com\\.liferay\\.workspace[\'\"].*", Pattern.MULTILINE | Pattern.DOTALL);

	public final String SETTINGS_GRADLE_FILE_NAME = "settings.gradle";

	public final File _workspaceCacheFile = new File(System.getProperty("user.home"), DEFAULT_WORKSPACE_CACHE_FILE);

}