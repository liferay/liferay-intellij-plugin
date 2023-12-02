/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayCore;
import com.liferay.ide.idea.core.ProductInfo;
import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.core.WorkspaceProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.file.Files;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.plugins.gradle.settings.GradleExtensionsSettings;

/**
 * @author Terry Jia
 * @author Simon Jiang
 * @author Ethan Sun
 */
public interface LiferayWorkspaceSupport {

	public static Map<String, ProductInfo> getProductInfos() {
		Gson gson = new Gson();
		TypeToken<Map<String, ProductInfo>> typeToken = new TypeToken<>() {
		};

		try (JsonReader jsonReader = new JsonReader(Files.newBufferedReader(_workspaceCacheFile.toPath()))) {
			return gson.fromJson(jsonReader, typeToken.getType());
		}
		catch (Exception exception) {
			File bladeJar = BladeCLI.getBladeJar(BladeCLI.getBladeJarVersion(null));

			if (bladeJar != null) {
				try (ZipFile zipFile = new ZipFile(bladeJar)) {
					Enumeration<? extends ZipEntry> entries = zipFile.entries();

					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();

						String entryName = entry.getName();

						if (entryName.equals(".product_info.json")) {
							try (InputStream resourceAsStream = zipFile.getInputStream(entry)) {
								if (Objects.nonNull(resourceAsStream)) {
									try (JsonReader jsonReader = new JsonReader(
											new InputStreamReader(resourceAsStream))) {

										return gson.fromJson(jsonReader, typeToken.getType());
									}
								}
							}
						}
					}
				}
				catch (IOException ioException) {
					_logger.error(ioException);
				}
			}
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

	public static boolean isValidGradleWorkspaceProject(@NotNull Project project) {
		return isValidGradleWorkspaceLocation(project.getBasePath());
	}

	public static boolean isValidMavenWorkspaceProject(@NotNull Project project) {
		try {
			MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);

			if (!mavenProjectsManager.isMavenizedProject()) {
				return false;
			}

			VirtualFile workspaceVirtualFile = getWorkspaceVirtualFile(project);

			if (workspaceVirtualFile == null) {
				return false;
			}

			Application application = ApplicationManager.getApplication();

			MavenProject mavenWorkspaceProject = application.runReadAction(
				(Computable<MavenProject>)() -> mavenProjectsManager.findContainingProject(workspaceVirtualFile));

			if (mavenWorkspaceProject == null) {
				return false;
			}

			MavenPlugin liferayWorkspacePlugin = mavenWorkspaceProject.findPlugin(
				"com.liferay", "com.liferay.portal.tools.bundle.support");

			if (liferayWorkspacePlugin != null) {
				return true;
			}
		}
		catch (Exception exception) {
			return false;
		}

		return false;
	}

	public static boolean isValidWorkspaceLocation(Project project) {
		if ((project != null) && (isValidGradleWorkspaceProject(project) || isValidMavenWorkspaceProject(project))) {
			return true;
		}

		return false;
	}

	public static boolean isWarCoreExtProject(Project project, Module module) {
		GradleExtensionsSettings.Settings settings = GradleExtensionsSettings.getInstance(project);

		GradleExtensionsSettings.GradleExtensionsData gradleExtensionsData = settings.getExtensionsFor(module);

		if (Objects.isNull(gradleExtensionsData)) {
			return false;
		}

		Map<String, GradleExtensionsSettings.GradleTask> tasksMap = gradleExtensionsData.tasksMap;

		GradleExtensionsSettings.GradleTask value = tasksMap.get("buildExtInfo");

		return Objects.nonNull(value);
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

	public default List<Module> getWarCoreExtModules(Project project) {
		List<Module> warCoreExtModules = new ArrayList<>();

		VirtualFile moduleExtDirFile = getModuleExtDirFile(project);

		if (Objects.isNull(moduleExtDirFile)) {
			return warCoreExtModules;
		}

		VirtualFile[] extDirVirtualFiles = moduleExtDirFile.getChildren();

		for (VirtualFile extVirtualFile : extDirVirtualFiles) {
			Module module = ModuleUtil.findModuleForFile(extVirtualFile, project);

			if (isWarCoreExtProject(project, module)) {
				warCoreExtModules.add(module);
			}
		}

		return warCoreExtModules;
	}

	public default String getWorkspaceModuleDir(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return null;
		}

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

	public Logger _logger = Logger.getInstance(LiferayWorkspaceSupport.class);
	public final File _workspaceCacheFile = new File(System.getProperty("user.home"), DEFAULT_WORKSPACE_CACHE_FILE);

}