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

import com.google.common.collect.ListMultimap;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.JavaHomeFinder;
import com.intellij.openapi.util.text.StringUtil;

import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.GradleDependency;
import com.liferay.ide.idea.util.GradleDependencyUpdater;
import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.PropertiesUtil;

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

		List<String> javaHomePaths = JavaHomeFinder.suggestHomePaths();

		File javaHomeFile = null;

		if (javaHomePaths.isEmpty()) {
			String pathEnv = System.getenv("PATH");

			String[] paths = pathEnv.split(Pattern.quote(File.pathSeparator));

			for (String pathValue : paths) {
				Path path = Paths.get(pathValue);

				Path javaPath = path.resolve("java");

				if (Files.exists(javaPath)) {
					javaHomeFile = javaPath.toFile();

					javaHomeFile = javaHomeFile.getParentFile();

					javaHomeFile = javaHomeFile.getParentFile();

					break;
				}
			}

			if (javaHomeFile == null) {
				javaHomeFile = new File(System.getProperty("java.home"));
			}
		}
		else {
			javaHomeFile = new File(javaHomePaths.get(0));
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

			GradleConnector.newConnector(
			).forProjectDirectory(
				file
			).connect(
			).newBuild(
			).setJavaHome(
				javaHomeFile
			).addArguments(
				"--rerun-tasks"
			).forTasks(
				"dependencyManagement"
			).setStandardOutput(
				outputStream
			).run();

			String output = outputStream.toString();

			String taskOutputInfo;

			if (CoreUtil.compareVersions(
					new Version(GradleUtil.getWorkspacePluginVersion(project)), new Version("2.2.4")) < 0) {

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
				catch (IOException ioe) {
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
			ProductInfo productInfo = getWorkspaceProductInfo();

			if (Objects.nonNull(productInfo)) {
				targetPlatformVersion = productInfo.getTargetPlatformVersion();
			}
		}

		if (!CoreUtil.isNullOrEmpty(targetPlatformVersion) && targetPlatformVersion.contains("-")) {
			targetPlatformVersion = targetPlatformVersion.substring(0, targetPlatformVersion.indexOf("-"));
		}

		return targetPlatformVersion;
	}

	@Override
	public String[] getWorkspaceModuleDirs() {
		if (CoreUtil.compareVersions(
				Version.parseVersion(GradleUtil.getWorkspacePluginVersion(project)), new Version("2.5.0")) < 0) {

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
	public ProductInfo getWorkspaceProductInfo() {
		String workspaceProductKey = getWorkspaceProperty(WorkspaceConstants.WORKSPACE_PRODUCT_PROPERTY, null);

		if (CoreUtil.isNullOrEmpty(workspaceProductKey)) {
			return null;
		}

		Map<String, ProductInfo> productInfos = LiferayWorkspaceSupport.getProductInfos(project);

		if (Objects.nonNull(productInfos)) {
			return productInfos.get(workspaceProductKey);
		}

		return null;
	}

	@Override
	public String getWorkspaceProperty(String key, String defaultValue) {
		File gradleProperties = new File(project.getBasePath(), "gradle.properties");

		if (gradleProperties.exists()) {
			Properties properties = PropertiesUtil.loadProperties(gradleProperties);

			if (properties == null) {
				return defaultValue;
			}

			return properties.getProperty(key, defaultValue);
		}

		return null;
	}

	@Override
	public String[] getWorkspaceWarDirs() {
		if (CoreUtil.compareVersions(
				Version.parseVersion(GradleUtil.getWorkspacePluginVersion(project)), new Version("2.5.0")) < 0) {

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
		catch (IOException ioe) {
		}

		String pluginVersion = Optional.ofNullable(
			gradleDependencyUpdater
		).flatMap(
			updater -> {
				ListMultimap<String, GradleDependency> dependencies = updater.getAllDependencies();

				List<GradleDependency> artifacts = new ArrayList<>(dependencies.values());

				return artifacts.stream(
				).filter(
					artifact -> Objects.equals("com.liferay", artifact.getGroup())
				).filter(
					artifact -> Objects.equals("com.liferay.gradle.plugins.workspace", artifact.getName())
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