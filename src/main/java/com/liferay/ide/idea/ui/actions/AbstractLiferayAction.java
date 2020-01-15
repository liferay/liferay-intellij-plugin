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
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
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
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import java.util.List;
import java.util.Objects;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Simon Jiang
 * @author Ethan Sun
 */
public abstract class AbstractLiferayAction extends AnAction {

	public AbstractLiferayAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	@Override
	public void actionPerformed(final AnActionEvent anActionEvent) {
		Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);

		DumbService dumbService = DumbService.getInstance(project);

		dumbService.suspendIndexingAndRun("Start execute action", () -> _perform(anActionEvent, project));
	}

	@Override
	public void update(AnActionEvent anActionEvent) {
		super.update(anActionEvent);

		Presentation eventPresentation = anActionEvent.getPresentation();

		eventPresentation.setEnabledAndVisible(isEnabledAndVisible(anActionEvent));
	}

	@Nullable
	protected abstract RunnerAndConfigurationSettings doExecute(AnActionEvent anActionEvent);

	protected ProgressExecutionMode getProgressMode() {
		return ProgressExecutionMode.IN_BACKGROUND_ASYNC;
	}

	@Nullable
	protected VirtualFile getVirtualFile(AnActionEvent anActionEvent) {
		return anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
	}

	protected VirtualFile getWorkingDirectory(AnActionEvent anActionEvent) {
		VirtualFile virtualFile = getVirtualFile(anActionEvent);

		ProjectRootManager projectRootManager = ProjectRootManager.getInstance(anActionEvent.getProject());

		ProjectFileIndex projectFileIndex = projectRootManager.getFileIndex();

		Module module = projectFileIndex.getModuleForFile(virtualFile);

		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		ModifiableRootModel modifiableRootModel = moduleRootManager.getModifiableModel();

		VirtualFile[] contentRootsVirtualFiles = modifiableRootModel.getContentRoots();

		assert contentRootsVirtualFiles[0] != null;

		return contentRootsVirtualFiles[0];
	}

	protected void handleProcessStarted(Project project) {
		_refreshProjectView(project);
	}

	protected void handleProcessTerminated(Project project) {
		_refreshProjectView(project);
	}

	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		return true;
	}

	private void _perform(AnActionEvent anActionEvent, Project project) {
		RunnerAndConfigurationSettings runnerAndConfigurationSettings = doExecute(anActionEvent);

		RunManager runManager = RunManager.getInstance(project);

		RunnerAndConfigurationSettings existingConfigurationSettings = runManager.findConfigurationByName(
			(runnerAndConfigurationSettings == null) ? null : runnerAndConfigurationSettings.getName());

		if (existingConfigurationSettings == null) {
			runManager.setTemporaryConfiguration(runnerAndConfigurationSettings);
		}
		else {
			runManager.setSelectedConfiguration(existingConfigurationSettings);
		}

		RunContentManager runContentManager = RunContentManager.getInstance(project);

		List<RunContentDescriptor> allDescriptors = runContentManager.getAllDescriptors();

		allDescriptors.forEach(
			descriptor -> {
				String displayName = descriptor.getDisplayName();

				ProcessHandler processHandler = descriptor.getProcessHandler();

				boolean processTerminated = processHandler.isProcessTerminated();

				if ((Objects.equals("Gradle." + displayName, existingConfigurationSettings.getUniqueID()) &&
					 !processTerminated) ||
					Objects.equals(
						existingConfigurationSettings.getUniqueID(), "Gradle." + project.getName() + " [watch]")) {

					processHandler.destroyProcess();

					Content content = descriptor.getAttachedContent();

					ContentManager contentManager = content.getManager();

					contentManager.removeContent(descriptor.getAttachedContent(), true);
				}
			});

		MessageBus messageBus = project.getMessageBus();

		MessageBusConnection messageBusConnection = messageBus.connect(project);

		messageBusConnection.subscribe(
			ExecutionManager.EXECUTION_TOPIC,
			new ExecutionListener() {

				public void processStarted(
					@NotNull String executorIdLocal, @NotNull ExecutionEnvironment executionEnvironment,
					@NotNull ProcessHandler processHandler) {

					handleProcessStarted(project);
				}

				public void processTerminated(
					@NotNull String executorIdLocal, @NotNull ExecutionEnvironment executionEnvironment,
					@NotNull ProcessHandler processHandler, int exitCode) {

					handleProcessTerminated(project);
				}

			});
	}

	private void _refreshProjectView(Project project) {
		ProjectView projectView = ProjectView.getInstance(project);

		projectView.refresh();
	}

}