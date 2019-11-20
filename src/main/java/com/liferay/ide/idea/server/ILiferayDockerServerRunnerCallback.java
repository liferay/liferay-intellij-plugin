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

import static com.liferay.ide.idea.util.GradleUtil.getModel;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.model.Container;

import com.google.common.collect.Lists;

import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemProcessHandler;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunnableState;
import com.intellij.openapi.project.ProjectUtil;

import com.liferay.blade.gradle.tooling.ProjectInfo;
import com.liferay.ide.idea.util.LiferayDockerClient;
import com.liferay.ide.idea.util.ListUtil;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * @author Simon Jiang
 */
public interface ILiferayDockerServerRunnerCallback {

	public default void dockerSeverStopHandler(
		ProcessHandler processHandler, @NotNull RunProfileState runProfileState,
		@NotNull ExecutionEnvironment environment) {

		Application application = ApplicationManager.getApplication();

		application.invokeLater(
			new Runnable() {

				@Override
				public void run() {
					try {
						if (processHandler instanceof ExternalSystemProcessHandler) {
							ExternalSystemProcessHandler exProcessHandler =
								(ExternalSystemProcessHandler)processHandler;

							exProcessHandler.addProcessListener(
								new ProcessAdapter() {

									@Override
									public void processTerminated(@NotNull ProcessEvent event) {
										ProcessHandler handler = event.getProcessHandler();

										ExternalSystemProcessHandler exHandler = (ExternalSystemProcessHandler)handler;

										String executionName = exHandler.getExecutionName();

										if (executionName.contains("Liferay Docker") &&
											(runProfileState instanceof
													ExternalSystemRunnableState)) {

											try (DockerClient dockerClient = LiferayDockerClient.getDockerClient()) {
												ProjectInfo projectInfo = getModel(
													ProjectInfo.class,
													ProjectUtil.guessProjectDir(environment.getProject()));

												ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();

												listContainersCmd.withNameFilter(
													Lists.newArrayList(projectInfo.getDockerContainerId()));
												listContainersCmd.withLimit(1);

												List<Container> containers = listContainersCmd.exec();

												if (ListUtil.isNotEmpty(containers)) {
													Container container = containers.get(0);

													StopContainerCmd stopContainerCmd = dockerClient.stopContainerCmd(
														container.getId());

													stopContainerCmd.exec();
												}
											}
											catch (Exception e) {
											}
										}
									}

								});
						}
					}
					catch (Exception e) {
					}
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