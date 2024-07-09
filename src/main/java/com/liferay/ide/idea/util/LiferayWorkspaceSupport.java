/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayCore;
import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.core.WorkspaceProvider;
import com.liferay.release.util.ReleaseEntry;
import com.liferay.release.util.ReleaseUtil;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.plugins.gradle.settings.GradleExtensionsSettings;

import org.osgi.framework.Version;

/**
 * @author Terry Jia
 * @author Simon Jiang
 * @author Ethan Sun
 * @author Drew Brokke
 */
public class LiferayWorkspaceSupport {

	public static String getHomeDir(Project project) {
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

	public static boolean getIndexSources(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return false;
		}

		return workspaceProvider.getIndexSources();
	}

	@Nullable
	public static String getLiferayProductGroupVersion(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return null;
		}

		return workspaceProvider.getLiferayProductGroupVersion();
	}

	public static Version getLiferayProductVersionObject(Project project) {
		String liferayProductGroupVersion = getLiferayProductGroupVersion(project);

		if (liferayProductGroupVersion == null) {
			return Version.emptyVersion;
		}

		return Version.parseVersion(liferayProductGroupVersion.replace("q", ""));
	}

	@Nullable
	public static String getMavenProperty(Project project, String key, String defaultValue) {
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
	public static VirtualFile getModuleExtDirFile(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return null;
		}

		if (!workspaceProvider.isGradleWorkspace()) {
			return null;
		}

		return workspaceProvider.getModuleExtDirFile();
	}

	public static String[] getProductGroupVersions() {
		if (_productGroupVersions == null) {
			Stream<ReleaseEntry> releaseEntryStream = getReleaseEntryStream();

			_productGroupVersions = releaseEntryStream.map(
				ReleaseEntry::getProductGroupVersion
			).distinct(
			).toArray(
				String[]::new
			);
		}

		return _productGroupVersions;
	}

	public static String[] getProductVersions(boolean showAll) {
		Stream<ReleaseEntry> releaseEntryStream = getReleaseEntryStream();

		return releaseEntryStream.filter(
			releaseEntry -> {
				if (showAll) {
					return true;
				}

				return releaseEntry.isPromoted();
			}
		).map(
			ReleaseEntry::getReleaseKey
		).toArray(
			String[]::new
		);
	}

	@Nullable
	public static ReleaseEntry getReleaseEntry(String product, String targetPlatform) throws IOException {
		Stream<ReleaseEntry> releaseEntryStream = getReleaseEntryStream();

		return releaseEntryStream.filter(
			releaseEntry -> Objects.equals(releaseEntry.getProduct(), product)
		).filter(
			releaseEntry -> Objects.equals(releaseEntry.getTargetPlatformVersion(), targetPlatform)
		).findFirst(
		).orElse(
			null
		);
	}

	public static Stream<ReleaseEntry> getReleaseEntryStream() {
		return ReleaseUtil.getReleaseEntryStream();
	}

	@Nullable
	public static String getTargetPlatformVersion(Project project) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return null;
		}

		return workspaceProvider.getTargetPlatformVersion();
	}

	public static List<Module> getWarCoreExtModules(Project project) {
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

	public static String getWorkspaceModuleDir(Project project) {
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

	public static String getWorkspaceProperty(Project project, String key, String defaultValue) {
		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return null;
		}

		return workspaceProvider.getWorkspaceProperty(key, defaultValue);
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

		File buildGradle = new File(workspaceDir, _BUILD_GRADLE_FILE_NAME);
		File settingsGradle = new File(workspaceDir, _SETTINGS_GRADLE_FILE_NAME);
		File gradleProperties = new File(workspaceDir, _GRADLE_PROPERTIES_FILE_NAME);

		if (!(buildGradle.exists() && settingsGradle.exists() && gradleProperties.exists())) {
			return false;
		}

		String settingsContent = FileUtil.readContents(settingsGradle, true);

		Matcher matcher = _patternWorkspacePlugin.matcher(settingsContent);

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

	private static final String _BUILD_GRADLE_FILE_NAME = "build.gradle";

	private static final String _GRADLE_PROPERTIES_FILE_NAME = "gradle.properties";

	private static final String _SETTINGS_GRADLE_FILE_NAME = "settings.gradle";

	private static final Pattern _patternWorkspacePlugin = Pattern.compile(
		".*apply.*plugin.*:.*[\'\"]com\\.liferay\\.workspace[\'\"].*", Pattern.MULTILINE | Pattern.DOTALL);
	private static String[] _productGroupVersions;

}