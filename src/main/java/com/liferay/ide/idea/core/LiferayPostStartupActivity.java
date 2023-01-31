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

import com.google.common.collect.Lists;

import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.execution.ParametersListUtil;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import com.liferay.ide.idea.server.LiferayDockerServerConfigurationType;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ProjectConfigurationUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenImportListener;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Simon Jiang
 */
public class LiferayPostStartupActivity implements DumbAware, LiferayWorkspaceSupport, StartupActivity.Background {

	@Override
	public void runActivity(@NotNull Project project) {
		VirtualFile projectDirVirtualFile = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (projectDirVirtualFile == null) {
			return;
		}

		projectDirVirtualFile.refresh(false, true);

		MessageBus messageBus = project.getMessageBus();

		MessageBusConnection messageBusConnection = messageBus.connect(project);

		StartupManager startupManager = StartupManager.getInstance(project);

		startupManager.runAfterOpened(
			() -> messageBusConnection.subscribe(
				ProjectDataImportListener.TOPIC,
				new ProjectDataImportListener() {

					@Override
					public void onImportFinished(@Nullable String projectPath) {
						Application application = ApplicationManager.getApplication();

						application.runReadAction(
							() -> {
								if (projectPath.equals(project.getBasePath())) {
									ProjectConfigurationUtil.configExcludedFolder(project, getHomeDir(project));
								}
							});
					}

				}));

		messageBusConnection.subscribe(
			MavenImportListener.TOPIC,
			(MavenImportListener)(projects, list) -> {
				Stream<Module> modulesStream = list.stream();

				modulesStream.map(
					module -> module.getProject()
				).filter(
					moduleProject -> moduleProject.equals(project)
				).distinct(
				).forEach(
					moduleProject -> {
						MavenProjectsManager mvnManager = MavenProjectsManager.getInstance(project);

						mvnManager.forceUpdateAllProjectsOrFindAllAvailablePomFiles();

						Application application = ApplicationManager.getApplication();

						application.runReadAction(
							() -> {
								String homeDir = getMavenProperty(
									moduleProject, WorkspaceConstants.MAVEN_HOME_DIR_PROPERTY,
									WorkspaceConstants.HOME_DIR_DEFAULT);

								ProjectConfigurationUtil.configExcludedFolder(moduleProject, homeDir);
							});
					}
				);
			});

		messageBusConnection.subscribe(
			RunManagerListener.TOPIC,
			new RunManagerListener() {

				@Override
				public void runConfigurationRemoved(@NotNull RunnerAndConfigurationSettings settings) {
					ConfigurationType configurationType = settings.getType();

					if (LiferayDockerServerConfigurationType.id.equals(configurationType.getId())) {
						ExternalSystemTaskExecutionSettings externalSystemTaskExecutionSettings =
							new ExternalSystemTaskExecutionSettings();

						CommandLineParser gradleCmdParser = new CommandLineParser();

						List<String> taskNames = Lists.newArrayList("removeDockerContainer", "cleanDockerImage");

						final List<String> tasks = taskNames.stream(
						).flatMap(
							s -> ParametersListUtil.parse(
								s, false, true
							).stream()
						).collect(
							Collectors.toList()
						);

						ParsedCommandLine parsedCommandLine = gradleCmdParser.parse(tasks);

						externalSystemTaskExecutionSettings.setExternalProjectPath(project.getBasePath());
						externalSystemTaskExecutionSettings.setExternalSystemIdString(
							GradleConstants.SYSTEM_ID.toString());
						externalSystemTaskExecutionSettings.setTaskNames(parsedCommandLine.getExtraArguments());

						ExternalSystemUtil.runTask(
							externalSystemTaskExecutionSettings, DefaultRunExecutor.EXECUTOR_ID, project,
							GradleConstants.SYSTEM_ID, null, ProgressExecutionMode.IN_BACKGROUND_ASYNC, false);
					}
				}

			});
	}

}