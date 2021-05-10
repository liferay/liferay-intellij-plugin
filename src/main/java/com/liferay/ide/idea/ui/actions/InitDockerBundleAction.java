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

package com.liferay.ide.idea.ui.actions;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import com.intellij.util.execution.ParametersListUtil;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.server.LiferayDockerServerConfigurationProducer;
import com.liferay.ide.idea.server.LiferayDockerServerConfigurationType;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.util.List;
import java.util.Objects;

import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Simon Jiang
 * @author Seiphon Wang
 */
public class InitDockerBundleAction extends AbstractLiferayGradleTaskAction implements LiferayWorkspaceSupport {

	public InitDockerBundleAction() {
		super("InitDockerBundle", "Run init docker Bundle task", LiferayIcons.LIFERAY_ICON, "createDockerContainer");
	}

	@Override
	protected void afterTask(Project project) {
		List<RunConfigurationProducer<?>> producers = LiferayDockerServerConfigurationProducer.getProducers(project);

		for (RunConfigurationProducer producer : producers) {
			ConfigurationType configurationType = producer.getConfigurationType();

			if (Objects.equals(LiferayDockerServerConfigurationType.id, configurationType.getId())) {
				RunManager runManager = RunManager.getInstance(project);

				RunnerAndConfigurationSettings configuration = runManager.findConfigurationByTypeAndName(
					configurationType, project.getName() + "-docker-server");

				if (configuration == null) {
					configuration = runManager.createConfiguration(
						project.getName() + "-docker-server", producer.getConfigurationFactory());

					runManager.addConfiguration(configuration);
				}

				runManager.setSelectedConfiguration(configuration);
			}
		}
	}

	@Override
	protected void beforeTask(Project project) {
		try {
			ExternalSystemTaskExecutionSettings settings = new ExternalSystemTaskExecutionSettings();

			CommandLineParser gradleCmdParser = new CommandLineParser();

			ParsedCommandLine parsedCommandLine = gradleCmdParser.parse(
				ParametersListUtil.parse("removeDockerContainer", true));

			settings.setExternalProjectPath(project.getBasePath());
			settings.setExternalSystemIdString(GradleConstants.SYSTEM_ID.toString());
			settings.setTaskNames(parsedCommandLine.getExtraArguments());

			ExternalSystemUtil.runTask(
				settings, DefaultRunExecutor.EXECUTOR_ID, project, GradleConstants.SYSTEM_ID,
				new TaskCallback() {

					@Override
					public void onFailure() {
					}

					@Override
					public void onSuccess() {
						ExternalSystemUtil.runTask(
							externalTaskExecutionInfo.getSettings(), externalTaskExecutionInfo.getExecutorId(), project,
							GradleConstants.SYSTEM_ID,
							new TaskCallback() {

								@Override
								public void onFailure() {
								}

								@Override
								public void onSuccess() {
									afterTask(project);
								}

							},
							ProgressExecutionMode.IN_BACKGROUND_ASYNC, true);
					}

				},
				ProgressExecutionMode.IN_BACKGROUND_ASYNC, true);
		}
		catch (Exception e) {
			_logger.error(e);
		}
	}

	@Nullable
	@Override
	protected void doExecute(
		AnActionEvent anActionEvent, RunnerAndConfigurationSettings runnerAndConfigurationSettings) {

		Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);

		beforeTask(project);
	}

	private static final Logger _logger = Logger.getInstance(InitDockerBundleAction.class);

}