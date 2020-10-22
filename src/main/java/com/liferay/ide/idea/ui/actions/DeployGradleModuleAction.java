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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.io.File;

/**
 * @author Andy Wu
 * @author Simon Jiang
 */
public class DeployGradleModuleAction extends AbstractLiferayGradleTaskAction implements LiferayWorkspaceSupport {

	public DeployGradleModuleAction() {
		super("Deploy", "Run deploy task", LiferayIcons.LIFERAY_ICON, "deploy");
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		Project project = anActionEvent.getProject();

		VirtualFile baseDir = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (baseDir == null) {
			return false;
		}

		VirtualFile gradleFile = baseDir.findChild("build.gradle");

		VirtualFile virtualFile = getVirtualFile(anActionEvent);

		if ((virtualFile != null) && (gradleFile != null) &&
			ProjectRootsUtil.isModuleContentRoot(virtualFile, project) && !baseDir.equals(virtualFile) &&
			FileUtil.exists(new File(project.getBasePath(), getHomeDir(project)))) {

			return true;
		}

		return false;
	}

}