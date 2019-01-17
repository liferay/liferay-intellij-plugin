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

import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

import icons.LiferayIcons;

import java.io.File;

/**
 * @author Andy Wu
 */
public class DeployGradleModuleAction extends AbstractLiferayGradleTaskAction {

	public DeployGradleModuleAction() {
		super("Deploy", "Run deploy task", LiferayIcons.LIFERAY_ICON, "deploy");
	}

	@Override
	@SuppressWarnings("deprecation")
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		Project project = anActionEvent.getProject();
		VirtualFile virtualFile = getVirtualFile(anActionEvent);

		VirtualFile baseDir = project.getBaseDir();

		VirtualFile gradleFile = baseDir.findChild("build.gradle");

		if ((virtualFile != null) && (gradleFile != null) &&
			ProjectRootsUtil.isModuleContentRoot(virtualFile, project) && !baseDir.equals(virtualFile) &&
			new File(LiferayWorkspaceUtil.getHomeDir(project.getBasePath())).exists()) {

			return true;
		}

		return false;
	}

}