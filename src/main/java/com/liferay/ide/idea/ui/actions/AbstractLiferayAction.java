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
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ServerUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Simon Jiang
 * @author Ethan Sun
 */
public abstract class AbstractLiferayAction extends AnAction implements LiferayWorkspaceSupport {

	public AbstractLiferayAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	@Override
	public void actionPerformed(final AnActionEvent anActionEvent) {
		Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);

		SwingUtilities.invokeLater(
			() -> {
				Application application = ApplicationManager.getApplication();

				application.invokeLater(() -> _perform(anActionEvent, project));
			});
	}

	public Set<Module> getServiceBuilderModules(AnActionEvent anActionEvent) {
		Set<Module> result = new HashSet<>();

		VirtualFile virtualFile = getVirtualFile(anActionEvent);

		if (Objects.isNull(virtualFile)) {
			return result;
		}

		Project project = anActionEvent.getProject();

		if (Objects.isNull(project)) {
			return result;
		}

		VfsUtil.visitChildrenRecursively(
			virtualFile,
			new VirtualFileVisitor<Void>(VirtualFileVisitor.NO_FOLLOW_SYMLINKS, VirtualFileVisitor.limit(5)) {

				@NotNull
				@Override
				public Result visitFileEx(@NotNull VirtualFile file) {
					if (!file.isDirectory() && Objects.equals(file.getName(), "service.xml")) {
						Module moduleForFile = ModuleUtil.findModuleForFile(file, project);

						if (Objects.nonNull(moduleForFile)) {
							result.add(moduleForFile);
						}

						return SKIP_CHILDREN;
					}

					Module moduleForFile = ModuleUtil.findModuleForFile(file, project);

					if (Objects.isNull(moduleForFile)) {
						return SKIP_CHILDREN;
					}

					boolean moduleDir = ModuleUtil.isModuleDir(moduleForFile, file);

					if (moduleDir && Objects.nonNull(file.findChild("service.xml"))) {
						result.add(moduleForFile);

						return SKIP_CHILDREN;
					}

					return CONTINUE;
				}

			});

		return result;
	}

	@Override
	public void update(AnActionEvent anActionEvent) {
		super.update(anActionEvent);

		Presentation eventPresentation = anActionEvent.getPresentation();

		eventPresentation.setEnabledAndVisible(isEnabledAndVisible(anActionEvent));
	}

	@Nullable
	protected abstract void doExecute(
		AnActionEvent anActionEvent, RunnerAndConfigurationSettings runnerAndConfigurationSettings);

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

		modifiableRootModel.dispose();

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

	@Nullable
	protected abstract RunnerAndConfigurationSettings processRunnerConfiguration(AnActionEvent anActionEvent);

	protected boolean verifyModuleDeploy(AnActionEvent anActionEvent) {
		Project project = anActionEvent.getProject();

		VirtualFile baseDir = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (baseDir == null) {
			return false;
		}

		VirtualFile virtualFile = getVirtualFile(anActionEvent);

		if ((virtualFile != null) && ProjectRootsUtil.isModuleContentRoot(virtualFile, project) &&
			!baseDir.equals(virtualFile)) {

			String homeDir = getHomeDir(project);

			if (Objects.isNull(homeDir)) {
				return false;
			}

			Path portalBundlePath = Paths.get(project.getBasePath(), homeDir);

			if (FileUtil.notExists(portalBundlePath) || Objects.isNull(ServerUtil.getPortalBundle(portalBundlePath))) {
				return false;
			}

			return true;
		}

		return false;
	}

	private void _perform(AnActionEvent anActionEvent, Project project) {
		RunnerAndConfigurationSettings runnerAndConfigurationSettings = processRunnerConfiguration(anActionEvent);

		doExecute(anActionEvent, runnerAndConfigurationSettings);

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