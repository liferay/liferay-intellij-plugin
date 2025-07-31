/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.core;

import com.google.common.collect.ListMultimap;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.GradleDependency;
import com.liferay.ide.idea.util.GradleDependencyUpdater;
import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.PropertiesUtil;
import com.liferay.release.util.ReleaseEntry;
import com.liferay.release.util.ReleaseUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;

import org.osgi.framework.Version;

/**
 * @author Simon Jiang
 */
public class LiferayGradleWorkspaceProvider extends AbstractWorkspaceProvider {

	public LiferayGradleWorkspaceProvider() {
	}

	public LiferayGradleWorkspaceProvider(Project project) {
		super(project);
	}

	@Override
	public boolean getIndexSources() {
		String result = getWorkspaceProperty(WorkspaceConstants.TARGET_PLATFORM_INDEX_SOURCES_PROPERTY, "false");

		return Boolean.parseBoolean(result);
	}

	@Override
	public String getLiferayHome() {
		return getWorkspaceProperty(WorkspaceConstants.HOME_DIR_PROPERTY, WorkspaceConstants.HOME_DIR_DEFAULT);
	}

	public List<String> getTargetPlatformDependencies() {
		String targetPlatformVersion = getTargetPlatformVersion();

		List<String> targetPlatformDependencyList = targetPlatformDependenciesMap.get(targetPlatformVersion);

		if ((targetPlatformDependencyList != null) && !targetPlatformDependencyList.isEmpty()) {
			return targetPlatformDependencyList;
		}

		File javaHomeFile = null;

		String pathEnv = System.getenv("PATH");

		String[] paths = pathEnv.split(Pattern.quote(File.pathSeparator));

		for (String pathValue : paths) {
			Path path = Paths.get(pathValue);

			Path javaPath = path.resolve("java");

			if (!Files.exists(javaPath)) {
				continue;
			}

			javaHomeFile = javaPath.toFile();

			javaHomeFile = javaHomeFile.getParentFile();

			javaHomeFile = javaHomeFile.getParentFile();

			break;
		}

		if (javaHomeFile == null) {
			javaHomeFile = new File(System.getProperty("java.home"));
		}

		if (!javaHomeFile.exists()) {
			return Collections.emptyList();
		}

		GradleProject workspaceGradleProject = GradleUtil.getWorkspaceGradleProject(project);

		DomainObjectSet<? extends GradleTask> tasksSet = workspaceGradleProject.getTasks();

		List<? extends GradleTask> tasksList = tasksSet.getAll();

		Optional<? extends GradleTask> dependencyManagementTask = tasksList.stream(
		).filter(
			task -> StringUtil.equals("dependencyManagement", task.getName())
		).filter(
			task -> workspaceGradleProject.equals(task.getProject())
		).findAny();

		List<String> list = new ArrayList<>();

		if (dependencyManagementTask.isPresent()) {
			File file = new File(project.getBasePath());

			OutputStream outputStream = new ByteArrayOutputStream();

			GradleConnector gradleConnector = GradleConnector.newConnector();

			gradleConnector = gradleConnector.forProjectDirectory(file);

			try (ProjectConnection connect = gradleConnector.connect()) {
				connect.newBuild(
				).setJavaHome(
					javaHomeFile
				).addArguments(
					"--rerun-tasks"
				).forTasks(
					"dependencyManagement"
				).setStandardOutput(
					outputStream
				).run();
			}

			String output = outputStream.toString();

			String taskOutputInfo;

			int ret = CoreUtil.compareVersions(
				new Version(GradleUtil.getWorkspacePluginVersion(project)), new Version("2.2.4"));

			if (ret < 0) {
				taskOutputInfo = "compileOnly - Dependency management for the compileOnly configuration";
			}
			else {
				taskOutputInfo = "> Task :dependencyManagement";
			}

			if (!CoreUtil.isNullOrEmpty(output) && !output.equals("")) {
				BufferedReader bufferedReader = new BufferedReader(new StringReader(output));

				String line;

				try {
					boolean start = false;

					while ((line = bufferedReader.readLine()) != null) {
						if (taskOutputInfo.equals(line)) {
							start = true;

							continue;
						}

						if (start) {
							if (StringUtil.equals(line.trim(), "")) {
								break;
							}

							list.add(line.trim());
						}
					}
				}
				catch (IOException ioException) {
				}
			}
		}

		targetPlatformDependenciesMap.put(targetPlatformVersion, list);

		return list;
	}

	@Override
	public String getTargetPlatformVersion() {
		String targetPlatformVersion = getWorkspaceProperty(WorkspaceConstants.TARGET_PLATFORM_VERSION_PROPERTY, null);

		if (CoreUtil.isNullOrEmpty(targetPlatformVersion)) {
			String workspaceProductKey = getWorkspaceProperty(WorkspaceConstants.WORKSPACE_PRODUCT_PROPERTY, null);

			if (!CoreUtil.isNullOrEmpty(workspaceProductKey)) {
				targetPlatformVersion = ReleaseUtil.getFromReleaseEntry(
					workspaceProductKey, ReleaseEntry::getTargetPlatformVersion);
			}
		}

		if (!CoreUtil.isNullOrEmpty(targetPlatformVersion) && targetPlatformVersion.contains("-")) {
			targetPlatformVersion = targetPlatformVersion.substring(0, targetPlatformVersion.indexOf("-"));
		}

		return targetPlatformVersion;
	}

	@Override
	public String[] getWorkspaceModuleDirs() {
		int ret = CoreUtil.compareVersions(
			Version.parseVersion(GradleUtil.getWorkspacePluginVersion(project)), new Version("2.5.0"));

		if (ret < 0) {
			String moduleDirs = getWorkspaceProperty(WorkspaceConstants.MODULES_DIR_PROPERTY, null);

			if (Objects.isNull(moduleDirs)) {
				return new String[] {WorkspaceConstants.MODULES_DIR_DEFAULT};
			}

			return moduleDirs.split(",");
		}

		String modulesDir = getWorkspaceProperty(
			WorkspaceConstants.MODULES_DIR_PROPERTY, WorkspaceConstants.MODULES_DIR_DEFAULT);

		if (StringUtil.equals(modulesDir, "*")) {
			return null;
		}

		return modulesDir.split(",");
	}

	@Override
	public String getWorkspaceProperty(String key, String defaultValue) {
		File gradleProperties = new File(project.getBasePath(), "gradle.properties");
		File gradleLocalProperties = new File(project.getBasePath(), "gradle-local.properties");

		Properties properties = new Properties();

		if (gradleProperties.exists()) {
			properties.putAll(PropertiesUtil.loadProperties(gradleProperties));
		}

		if (gradleLocalProperties.exists()) {
			properties.putAll(PropertiesUtil.loadProperties(gradleLocalProperties));
		}

		if (properties.isEmpty()) {
			return defaultValue;
		}

		return properties.getProperty(key, defaultValue);
	}

	@Override
	public String[] getWorkspaceWarDirs() {
		int ret = CoreUtil.compareVersions(
			Version.parseVersion(GradleUtil.getWorkspacePluginVersion(project)), new Version("2.5.0"));

		if (ret < 0) {
			String warDirs = getWorkspaceProperty(WorkspaceConstants.WARS_DIR_PROPERTY, null);

			if (Objects.isNull(warDirs)) {
				return new String[] {"wars"};
			}

			return warDirs.split(",");
		}

		String warDirs = getWorkspaceProperty(WorkspaceConstants.WARS_DIR_PROPERTY, null);

		if (Objects.nonNull(warDirs)) {
			return warDirs.split(",");
		}

		String modulesDir = getWorkspaceProperty(WorkspaceConstants.MODULES_DIR_PROPERTY, "modules");

		if (StringUtil.equals(modulesDir, "*")) {
			return null;
		}

		return modulesDir.split(",");
	}

	@Override
	public boolean isFlexibleLiferayWorkspace() {
		File settingsGradleFile = new File(project.getBasePath(), "settings.gradle");

		GradleDependencyUpdater gradleDependencyUpdater = null;

		try {
			gradleDependencyUpdater = new GradleDependencyUpdater(settingsGradleFile);
		}
		catch (IOException ioException) {
		}

		String pluginVersion = Optional.ofNullable(
			gradleDependencyUpdater
		).flatMap(
			updater -> {
				ListMultimap<String, GradleDependency> dependencies = updater.getAllDependencies();

				List<GradleDependency> artifacts = new ArrayList<>(dependencies.values());

				return artifacts.stream(
				).filter(
					artifact -> Objects.equals(artifact.getGroup(), "com.liferay")
				).filter(
					artifact -> Objects.equals(artifact.getName(), "com.liferay.gradle.plugins.workspace")
				).filter(
					artifact -> !CoreUtil.isNullOrEmpty(artifact.getVersion())
				).map(
					GradleDependency::getVersion
				).findFirst();
			}
		).orElseGet(
			() -> "2.2.4"
		);

		if (CoreUtil.compareVersions(new Version(pluginVersion), new Version("2.5.0")) >= 0) {
			return true;
		}

		return false;
	}

	public boolean isGradleWorkspace() {
		return true;
	}

	@Override
	public <T> T provide(Project project, Class<T> adapterType) {
		if (LiferayWorkspaceSupport.isValidGradleWorkspaceProject(project)) {
			return adapterType.cast(new LiferayGradleWorkspaceProvider(project));
		}

		return null;
	}

	public Map<String, List<String>> targetPlatformDependenciesMap = new HashMap<>();

}