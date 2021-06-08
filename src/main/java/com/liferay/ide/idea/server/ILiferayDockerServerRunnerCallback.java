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

package com.liferay.ide.idea.server;

import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemProcessHandler;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunnableState;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import com.intellij.util.execution.ParametersListUtil;

import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Simon Jiang
 * @author Seiphon Wang
 */
public interface ILiferayDockerServerRunnerCallback {

	public default void registerDockerSeverStopHandler(
		ProcessHandler processHandler, @NotNull RunProfileState runProfileState,
		@NotNull ExecutionEnvironment environment) {

		Application application = ApplicationManager.getApplication();

		application.invokeLater(
			() -> {
				try {
					if (processHandler instanceof ExternalSystemProcessHandler) {
						ExternalSystemProcessHandler exProcessHandler = (ExternalSystemProcessHandler)processHandler;

						exProcessHandler.addProcessListener(
							new ProcessAdapter() {

								@Override
								public void processTerminated(@NotNull ProcessEvent event) {
									ProcessHandler handler = event.getProcessHandler();

									ExternalSystemProcessHandler exHandler = (ExternalSystemProcessHandler)handler;

									String executionName = exHandler.getExecutionName();

									if (executionName.contains("Liferay Docker") &&
										(runProfileState instanceof ExternalSystemRunnableState)) {

										Project project = environment.getProject();

										ExternalSystemTaskExecutionSettings settings =
											new ExternalSystemTaskExecutionSettings();

										CommandLineParser gradleCmdParser = new CommandLineParser();

										ParsedCommandLine parsedCommandLine = gradleCmdParser.parse(
											ParametersListUtil.parse("stopDockerContainer", true));

										settings.setExternalProjectPath(project.getBasePath());
										settings.setExternalSystemIdString(GradleConstants.SYSTEM_ID.toString());
										settings.setTaskNames(parsedCommandLine.getExtraArguments());

										ExternalSystemUtil.runTask(
											settings, DefaultRunExecutor.EXECUTOR_ID, project,
											GradleConstants.SYSTEM_ID,
											new TaskCallback() {

												@Override
												public void onFailure() {
												}

												@Override
												public void onSuccess() {
												}

											},
											ProgressExecutionMode.IN_BACKGROUND_ASYNC, true);
									}
								}

							});
					}
				}
				catch (Exception exception) {
				}
			});

		processHandler.addProcessListener(
			new ProcessAdapter() {

				@Override
				public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed) {
					if (processHandler.equals(event.getProcessHandler()) &&
						(processHandler instanceof ExternalSystemProcessHandler)) {

						ExternalSystemProcessHandler exProcessHandler = (ExternalSystemProcessHandler)processHandler;

						String executionName = exProcessHandler.getExecutionName();

						if (executionName.contains("Liferay Docker")) {
							processHandler.destroyProcess();
						}
					}
				}

			});
	}

}