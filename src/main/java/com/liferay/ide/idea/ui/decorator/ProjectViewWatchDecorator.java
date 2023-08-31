/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.decorator;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTask;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskState;
import com.intellij.openapi.externalSystem.service.internal.ExternalSystemExecuteTaskTask;
import com.intellij.openapi.externalSystem.service.internal.ExternalSystemProcessingManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Simon Jiang
 */
public class ProjectViewWatchDecorator implements ProjectViewNodeDecorator {

	@Override
	@SuppressWarnings("rawtypes")
	public void decorate(ProjectViewNode node, PresentationData data) {
		Project project = node.getProject();

		if (project == null) {
			return;
		}

		VirtualFile virtualFile = node.getVirtualFile();

		if (virtualFile == null) {
			return;
		}

		String canonicalPath = virtualFile.getCanonicalPath();

		if (canonicalPath == null) {
			return;
		}

		if (ProjectRootsUtil.isModuleContentRoot(virtualFile, project)) {
			Application application = ApplicationManager.getApplication();

			ExternalSystemProcessingManager processingManager = application.getService(
				ExternalSystemProcessingManager.class);

			List<ExternalSystemTask> taskList = processingManager.findTasksOfState(
				GradleConstants.SYSTEM_ID, ExternalSystemTaskState.IN_PROGRESS);

			Stream<ExternalSystemTask> taskStream = taskList.stream();

			taskStream.filter(
				task -> task instanceof ExternalSystemExecuteTaskTask
			).map(
				task -> (ExternalSystemExecuteTaskTask)task
			).filter(
				task -> {
					List<String> tasksToExecute = task.getTasksToExecute();

					return tasksToExecute.contains("watch");
				}
			).map(
				ExternalSystemExecuteTaskTask::getExternalProjectPath
			).filter(
				externalProjectPath -> canonicalPath.equals(externalProjectPath)
			).forEach(
				externalProjectPath -> {
					String existedLocationString = data.getLocationString();

					if (existedLocationString != null) {
						data.setLocationString(existedLocationString + " [watching]");
					}
					else {
						data.setLocationString("[watching]");
					}
				}
			);
		}
	}

}