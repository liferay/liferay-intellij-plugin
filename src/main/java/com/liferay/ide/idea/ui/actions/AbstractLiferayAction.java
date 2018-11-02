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
	public void actionPerformed(final AnActionEvent event) {
		project = event.getRequiredData(CommonDataKeys.PROJECT);

		DumbService dumbService = DumbService.getInstance(project);

		dumbService.suspendIndexingAndRun("Start execute action", () -> _perform(event));
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

	protected VirtualFile getWorkingDirectory(AnActionEvent event) {
		VirtualFile virtualFile = getVirtualFile(event);

		ProjectRootManager projectRootManager = ProjectRootManager.getInstance(event.getProject());

		ProjectFileIndex projectFileIndex = projectRootManager.getFileIndex();

		Module module = projectFileIndex.getModuleForFile(virtualFile);

		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		ModifiableRootModel modifiableRootModel = moduleRootManager.getModifiableModel();

		VirtualFile[] roots = modifiableRootModel.getContentRoots();

		assert roots != null && roots[0] != null;

		return roots[0];
	}

	protected abstract void handleProcessStarted(
		@NotNull String executorIdLocal, @NotNull ExecutionEnvironment environmentLocal,
		@NotNull ProcessHandler handler);

	protected abstract void handleProcessTerminated(
		@NotNull String executorIdLocal, @NotNull ExecutionEnvironment environmentLocal,
		@NotNull ProcessHandler handler);

	protected void refreshProjectView() {
		ProjectView projectView = ProjectView.getInstance(project);

		projectView.refresh();
	}

	protected Project project;
	protected VirtualFile projectDir;

	private void _perform(AnActionEvent event) {
		RunnerAndConfigurationSettings configurationSetting = doExecute(event);

		RunManager runManager = RunManager.getInstance(project);

		RunnerAndConfigurationSettings existingConfiguration = runManager.findConfigurationByName(
			configurationSetting.getName());

		if (existingConfiguration == null) {
			runManager.setTemporaryConfiguration(configurationSetting);
		}
		else {
			runManager.setSelectedConfiguration(existingConfiguration);
		}

		MessageBus messageBus = project.getMessageBus();

		MessageBusConnection messageBusConnection = messageBus.connect(project);

		messageBusConnection.subscribe(
			ExecutionManager.EXECUTION_TOPIC,
			new ExecutionListener() {

				public void processStarted(
					@NotNull String executorIdLocal, @NotNull ExecutionEnvironment environmentLocal,
					@NotNull ProcessHandler handler) {

					handleProcessStarted(executorIdLocal, environmentLocal, handler);
				}

				public void processTerminated(
					@NotNull String executorIdLocal, @NotNull ExecutionEnvironment environmentLocal,
					@NotNull ProcessHandler handler, int exitCode) {

					handleProcessTerminated(executorIdLocal, environmentLocal, handler);
				}

			});
	}

}