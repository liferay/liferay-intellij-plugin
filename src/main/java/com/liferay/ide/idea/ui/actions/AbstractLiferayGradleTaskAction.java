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

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.model.execution.ExternalTaskExecutionInfo;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemNotificationManager;
import com.intellij.openapi.externalSystem.service.notification.NotificationCategory;
import com.intellij.openapi.externalSystem.service.notification.NotificationData;
import com.intellij.openapi.externalSystem.service.notification.NotificationSource;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.execution.ParametersListUtil;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import com.liferay.ide.idea.util.CoreUtil;

import org.gradle.cli.CommandLineArgumentException;
import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.service.execution.cmd.GradleCommandLineOptionsConverter;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import javax.swing.Icon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andy Wu
 * @author Simon Jiang
 */
public abstract class AbstractLiferayGradleTaskAction extends AnAction {

	public AbstractLiferayGradleTaskAction(
		@Nullable String text, @Nullable String description, @Nullable Icon icon, String taskName) {

		super(text, description, icon);

		_taskName = taskName;
	}

	@Override
	public void actionPerformed(final AnActionEvent event) {
		project = event.getRequiredData(CommonDataKeys.PROJECT);

		workingDirectory = getWorkingDirectory(event);

		if (CoreUtil.isNullOrEmpty(workingDirectory)) {
			return;
		}

		final ExternalTaskExecutionInfo taskExecutionInfo = _buildTaskExecutionInfo(
			project, workingDirectory, _taskName);

		if (taskExecutionInfo == null) {
			return;
		}

		ExternalSystemUtil.runTask(
			taskExecutionInfo.getSettings(), taskExecutionInfo.getExecutorId(), project, GradleConstants.SYSTEM_ID,
			new TaskCallback() {

				@Override
				public void onFailure() {
				}

				@Override
				public void onSuccess() {
					afterTask();
				}

			},
			getProgressMode(), true);

		RunnerAndConfigurationSettings configuration =
			ExternalSystemUtil.createExternalSystemRunnerAndConfigurationSettings(
				taskExecutionInfo.getSettings(), project, GradleConstants.SYSTEM_ID);

		if (configuration == null) {
			return;
		}

		RunManager runManager = RunManager.getInstance(project);

		RunnerAndConfigurationSettings existingConfiguration = runManager.findConfigurationByName(
			configuration.getName());

		if (existingConfiguration == null) {
			runManager.setTemporaryConfiguration(configuration);
		}
		else {
			runManager.setSelectedConfiguration(existingConfiguration);
		}

		MessageBus messageBus = project.getMessageBus();

		Disposable disposable = Disposer.newDisposable();

		MessageBusConnection connect = messageBus.connect(disposable);

		connect.subscribe(
			ExecutionManager.EXECUTION_TOPIC,
			new ExecutionListener() {

				public void processStarted(
					@NotNull final String executorIdLocal, @NotNull final ExecutionEnvironment environmentLocal,
					@NotNull final ProcessHandler handler) {

					if (!checkProcess(taskExecutionInfo, executorIdLocal, environmentLocal)) {
						return;
					}

					handleProcessStarted(handler);
				}

				public void processTerminated(
					@NotNull String executorIdLocal, @NotNull ExecutionEnvironment environmentLocal,
					@NotNull ProcessHandler handler, int exitCode) {

					if (!checkProcess(taskExecutionInfo, executorIdLocal, environmentLocal)) {
						return;
					}

					handleProcessTerminated(handler);
				}

			});
	}

	public void afterTask() {
	}

	public boolean continuous() {
		return false;
	}

	public boolean isEnabledAndVisible(AnActionEvent event) {
		return true;
	}

	@Override
	public void update(AnActionEvent event) {
		super.update(event);

		Presentation eventPresentation = event.getPresentation();

		eventPresentation.setEnabledAndVisible(isEnabledAndVisible(event));
	}

	protected boolean checkProcess(
		ExternalTaskExecutionInfo taskExecutionInfo, @NotNull final String executorIdLocal,
		@NotNull final ExecutionEnvironment environmentLocal) {

		RunnerAndConfigurationSettings runnerSettings = environmentLocal.getRunnerAndConfigurationSettings();

		RunConfiguration configuration = runnerSettings.getConfiguration();

		if (configuration instanceof ExternalSystemRunConfiguration) {
			ExternalSystemRunConfiguration runnerExternalSystemRunConfiguration =
				(ExternalSystemRunConfiguration)runnerSettings.getConfiguration();

			ExternalSystemTaskExecutionSettings runningTaskSettings =
				runnerExternalSystemRunConfiguration.getSettings();

			ExternalSystemTaskExecutionSettings taskRunnerSettings = taskExecutionInfo.getSettings();

			String externalPath = taskRunnerSettings.getExternalProjectPath();
			List<String> taskNames = taskRunnerSettings.getTaskNames();
			ProjectSystemId externalSystemId = taskRunnerSettings.getExternalSystemId();

			String executorId = taskExecutionInfo.getExecutorId();

			if (externalPath.equals(runningTaskSettings.getExternalProjectPath()) &&
				taskNames.equals(runningTaskSettings.getTaskNames()) &&
				externalSystemId.equals(runningTaskSettings.getExternalSystemId()) &&
				executorId.equals(executorIdLocal)) {

				return true;
			}
		}

		return false;
	}

	protected ProgressExecutionMode getProgressMode() {
		return ProgressExecutionMode.IN_BACKGROUND_ASYNC;
	}

	protected VirtualFile getVirtualFile(AnActionEvent event) {
		Object virtualFileObject = event.getData(CommonDataKeys.VIRTUAL_FILE);

		if ((virtualFileObject != null) && (virtualFileObject instanceof VirtualFile)) {
			return (VirtualFile)virtualFileObject;
		}

		return null;
	}

	protected String getWorkingDirectory(AnActionEvent event) {
		VirtualFile virtualFile = getVirtualFile(event);

		ProjectRootManager projectRootManager = ProjectRootManager.getInstance(event.getProject());

		ProjectFileIndex projectFileIndex = projectRootManager.getFileIndex();

		Module module = projectFileIndex.getModuleForFile(virtualFile);

		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		ModifiableRootModel modifiableRootModel = moduleRootManager.getModifiableModel();

		VirtualFile[] roots = modifiableRootModel.getContentRoots();

		assert roots != null && roots[0] != null;

		return roots[0].getCanonicalPath();
	}

	protected void handleProcessStarted(@NotNull ProcessHandler handler) {
		_refreshProjectView();
	}

	protected void handleProcessTerminated(@NotNull ProcessHandler handler) {
		_refreshProjectView();
	}

	protected Project project;
	protected String workingDirectory;

	private ExternalTaskExecutionInfo _buildTaskExecutionInfo(
		Project project, @NotNull String projectPath, @NotNull String fullCommandLine) {

		CommandLineParser gradleCmdParser = new CommandLineParser();

		GradleCommandLineOptionsConverter commandLineConverter = new GradleCommandLineOptionsConverter();

		commandLineConverter.configure(gradleCmdParser);

		ParsedCommandLine parsedCommandLine = gradleCmdParser.parse(ParametersListUtil.parse(fullCommandLine, true));

		try {
			Map<String, List<String>> optionsMap = commandLineConverter.convert(parsedCommandLine, new HashMap<>());

			List<String> systemProperties = optionsMap.remove("system-prop");

			String vmOptions =
				systemProperties == null ? "" : StringUtil.join(systemProperties, entry -> "-D" + entry, " ");

			String scriptParameters = StringUtil.join(
				optionsMap.entrySet(),
				entry -> {
					List<String> values = entry.getValue();
					String longOptionName = entry.getKey();

					if ((values != null) && !values.isEmpty()) {
						return StringUtil.join(values, entry1 -> "--" + longOptionName + ' ' + entry1, " ");
					}
					else {
						return "--" + longOptionName;
					}
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

	private void _refreshProjectView() {
		ProjectView projectView = ProjectView.getInstance(project);

		projectView.refresh();
	}

	private final String _taskName;

}