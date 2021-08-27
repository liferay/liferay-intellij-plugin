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

import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ServerUtil;

import java.util.List;
import java.util.Objects;

/**
 * @author Seiphon Wang
 */
public class DeployDockerModuleAction extends AbstractLiferayGradleTaskAction implements LiferayWorkspaceSupport {

	public DeployDockerModuleAction() {
		super("DockerDeploy", "Run docker deploy task", LiferayIcons.LIFERAY_ICON, "dockerDeploy");
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		if (super.isEnabledAndVisible(anActionEvent)) {
			Project project = anActionEvent.getProject();

			VirtualFile baseDir = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

			if (baseDir == null) {
				return false;
			}

			VirtualFile virtualFile = getVirtualFile(anActionEvent);

			Module module = ModuleUtil.findModuleForFile(virtualFile, project);

			List<Module> unSupportModules = getWarCoreExtProjects(project);

			if (Objects.nonNull(getModuleExtDirFile(project))) {
				unSupportModules.add(ModuleUtil.findModuleForFile(getModuleExtDirFile(project), project));
			}

			if ((virtualFile != null) && ProjectRootsUtil.isModuleContentRoot(virtualFile, project) &&
				!baseDir.equals(virtualFile) && ServerUtil.isDockerServerExist(project) &&
				!unSupportModules.contains(module)) {

				return true;
			}
		}

		return false;
	}

}