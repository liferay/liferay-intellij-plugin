/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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
import com.intellij.openapi.startup.ProjectActivity;
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

import kotlin.Unit;

import kotlin.coroutines.Continuation;

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
public class LiferayPostStartupActivity implements DumbAware, LiferayWorkspaceSupport, ProjectActivity {

	@Nullable
	@Override
	public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
		VirtualFile projectDirVirtualFile = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (projectDirVirtualFile == null) {
			return project;
		}

		projectDirVirtualFile.refresh(false, true);

		MessageBus messageBus = project.getMessageBus();

		MessageBusConnection messageBusConnection = messageBus.connect(project);

		messageBusConnection.subscribe(
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

			});

		Application application = ApplicationManager.getApplication();

		application.runReadAction(
			new Runnable() {

				@Override
				public void run() {
					ProjectConfigurationUtil.configExcludedFolder(project, getHomeDir(project));
				}

			});

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

		return project;
	}

}