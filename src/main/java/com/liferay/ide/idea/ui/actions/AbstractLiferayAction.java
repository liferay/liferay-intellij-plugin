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
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Simon Jiang
 */
public abstract class AbstractLiferayAction extends AnAction {

	public AbstractLiferayAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	@Override
	public void actionPerformed(final AnActionEvent anActionEvent) {
		project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);

		DumbService dumbService = DumbService.getInstance(project);

		dumbService.suspendIndexingAndRun("Start execute action", () -> _perform(anActionEvent));
	}

	public boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		return true;
	}

	@Override
	public void update(AnActionEvent anActionEvent) {
		super.update(anActionEvent);

		Presentation eventPresentation = anActionEvent.getPresentation();

		eventPresentation.setEnabledAndVisible(isEnabledAndVisible(anActionEvent));
	}

	protected abstract RunnerAndConfigurationSettings doExecute(AnActionEvent event);

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

	protected VirtualFile getWorkingDirectory(AnActionEvent anActionEvent) {
		VirtualFile virtualFile = getVirtualFile(anActionEvent);

		ProjectRootManager projectRootManager = ProjectRootManager.getInstance(anActionEvent.getProject());

		ProjectFileIndex projectFileIndex = projectRootManager.getFileIndex();

		Module module = projectFileIndex.getModuleForFile(virtualFile);

		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		ModifiableRootModel modifiableRootModel = moduleRootManager.getModifiableModel();

		VirtualFile[] contentRootsVirtualFiles = modifiableRootModel.getContentRoots();

		assert contentRootsVirtualFiles != null && contentRootsVirtualFiles[0] != null;

		return contentRootsVirtualFiles[0];
	}

	protected void handleProcessStarted() {
		_refreshProjectView();
	}

	protected void handleProcessTerminated() {
		_refreshProjectView();
	}

	protected Project project;
	protected VirtualFile projectDir;

	private void _perform(AnActionEvent anActionEvent) {
		RunnerAndConfigurationSettings runnerAndConfigurationSetting = doExecute(anActionEvent);

		RunManager runManager = RunManager.getInstance(project);

		RunnerAndConfigurationSettings existingConfigurationSettings = runManager.findConfigurationByName(
			runnerAndConfigurationSetting.getName());

		if (existingConfigurationSettings == null) {
			runManager.setTemporaryConfiguration(runnerAndConfigurationSetting);
		}
		else {
			runManager.setSelectedConfiguration(existingConfigurationSettings);
		}

		MessageBus messageBus = project.getMessageBus();

		MessageBusConnection messageBusConnection = messageBus.connect(project);

		messageBusConnection.subscribe(
			ExecutionManager.EXECUTION_TOPIC,
			new ExecutionListener() {

				public void processStarted(
					@NotNull String executorIdLocal, @NotNull ExecutionEnvironment environmentLocal,
					@NotNull ProcessHandler handler) {

					handleProcessStarted();
				}

				public void processTerminated(
					@NotNull String executorIdLocal, @NotNull ExecutionEnvironment environmentLocal,
					@NotNull ProcessHandler handler, int exitCode) {

					handleProcessTerminated();
				}

			});
	}

	private void _refreshProjectView() {
		ProjectView projectView = ProjectView.getInstance(project);

		projectView.refresh();
	}

}