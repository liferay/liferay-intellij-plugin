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

import com.google.common.collect.Lists;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.model.execution.ExternalTaskExecutionInfo;
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemNotificationManager;
import com.intellij.openapi.externalSystem.service.notification.NotificationCategory;
import com.intellij.openapi.externalSystem.service.notification.NotificationData;
import com.intellij.openapi.externalSystem.service.notification.NotificationSource;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ListUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.Icon;

import org.gradle.cli.CommandLineArgumentException;
import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.service.execution.cmd.GradleCommandLineOptionsConverter;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Andy Wu
 * @author Simon Jiang
 */
public abstract class AbstractLiferayGradleTaskAction extends AbstractLiferayAction {

	public AbstractLiferayGradleTaskAction(
		@Nullable String text, @Nullable String description, @Nullable Icon icon, String taskName) {

		super(text, description, icon);

		_taskName = taskName;
	}

	protected void afterTask(Project project) {
	}

	protected void beforeTask(Project project) {
	}

	protected boolean continuous() {
		return false;
	}

	@Nullable
	@Override
	protected void doExecute(
		AnActionEvent anActionEvent, RunnerAndConfigurationSettings runnerAndConfigurationSettings) {

		Project project = anActionEvent.getProject();

		beforeTask(project);

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
			getProgressMode(), true);
	}

	protected List<String> getGradleTasks() {
		return Lists.newArrayList(_taskName);
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		Project project = anActionEvent.getProject();

		if (!LiferayWorkspaceSupport.isValidGradleWorkspaceProject(project)) {
			return false;
		}

		VirtualFile virtualFile = getVirtualFile(anActionEvent);

		if ((project == null) || (virtualFile == null)) {
			return false;
		}

		Module module = ModuleUtil.findModuleForFile(virtualFile, project);

		if (Objects.isNull(module)) {
			return false;
		}

		return true;
	}

	@Nullable
	@Override
	protected RunnerAndConfigurationSettings processRunnerConfiguration(AnActionEvent anActionEvent) {
		final VirtualFile projectDir = getWorkingDirectory(anActionEvent);

		final String workingDirectory = projectDir.getCanonicalPath();

		if (CoreUtil.isNullOrEmpty(workingDirectory)) {
			return null;
		}

		List<String> gradleTasks = getGradleTasks();

		if (ListUtil.isEmpty(gradleTasks)) {
			return null;
		}

		Project project = anActionEvent.getProject();

		externalTaskExecutionInfo = _buildTaskExecutionInfo(project, workingDirectory, gradleTasks);

		if (externalTaskExecutionInfo == null) {
			return null;
		}

		return ExternalSystemUtil.createExternalSystemRunnerAndConfigurationSettings(
			externalTaskExecutionInfo.getSettings(), project, GradleConstants.SYSTEM_ID);
	}

	protected ExternalTaskExecutionInfo externalTaskExecutionInfo;

	private ExternalTaskExecutionInfo _buildTaskExecutionInfo(
		Project project, @NotNull String projectPath, @NotNull List<String> gradleTasks) {

		CommandLineParser gradleCmdParser = new CommandLineParser();

		GradleCommandLineOptionsConverter commandLineConverter = new GradleCommandLineOptionsConverter();

		commandLineConverter.configure(gradleCmdParser);

		ParsedCommandLine parsedCommandLine = gradleCmdParser.parse(gradleTasks);

		try {
			Map<String, List<String>> optionsMap = commandLineConverter.convert(parsedCommandLine, new HashMap<>());

			List<String> systemProperties = optionsMap.remove("system-prop");

			String vmOptions =
				(systemProperties == null) ? "" : StringUtil.join(systemProperties, entry -> "-D" + entry, " ");

			String scriptParameters = StringUtil.join(
				optionsMap.entrySet(),
				entry -> {
					List<String> values = entry.getValue();
					String longOptionName = entry.getKey();

					if ((values != null) && !values.isEmpty()) {
						return StringUtil.join(values, entry1 -> "--" + longOptionName + ' ' + entry1, " ");
					}

					return "--" + longOptionName;
				},
				" ");

			if (continuous()) {
				scriptParameters = scriptParameters + " --continuous";
			}

			ExternalSystemTaskExecutionSettings settings = new ExternalSystemTaskExecutionSettings();

			settings.setExternalProjectPath(projectPath);
			settings.setExternalSystemIdString(GradleConstants.SYSTEM_ID.toString());
			settings.setScriptParameters(scriptParameters);
			settings.setTaskNames(parsedCommandLine.getExtraArguments());
			settings.setVmOptions(vmOptions);

			return new ExternalTaskExecutionInfo(settings, DefaultRunExecutor.EXECUTOR_ID);
		}
		catch (CommandLineArgumentException clae) {
			NotificationData notificationData = new NotificationData(
				"<b>Command-line arguments cannot be parsed</b>", "<i>" + _taskName + "</i> \n" + clae.getMessage(),
				NotificationCategory.WARNING, NotificationSource.TASK_EXECUTION);

			notificationData.setBalloonNotification(true);

			ExternalSystemNotificationManager externalSystemNotificationManager =
				ExternalSystemNotificationManager.getInstance(project);

			externalSystemNotificationManager.showNotification(GradleConstants.SYSTEM_ID, notificationData);

			return null;
		}
	}

	private String _taskName;

}