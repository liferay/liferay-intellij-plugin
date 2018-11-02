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

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.util.IntellijUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

import icons.LiferayIcons;

import org.jetbrains.annotations.NotNull;

/**
 * @author Andy Wu
 * @author Simon Jiang
 */
public class InitBundleGradleAction extends AbstractLiferayGradleTaskAction {

	public InitBundleGradleAction() {
		super("InitBundle", "Run initBundle task", LiferayIcons.LIFERAY_ICON, "initBundle");
	}

	@Override
	public boolean isEnabledAndVisible(AnActionEvent event) {
		Project project = event.getProject();

		VirtualFile baseDir = project.getBaseDir();

		VirtualFile gradleFile = baseDir.findChild("build.gradle");

		if (baseDir.equals(getVirtualFile(event)) && (gradleFile != null)) {
			return true;
		}

		return false;
	}

	@Override
	protected void handleProcessTerminated(
		@NotNull String executorIdLocal, @NotNull ExecutionEnvironment environmentLocal,
		@NotNull ProcessHandler handler) {

		String homeDir = LiferayWorkspaceUtil.getHomeDir(project.getBasePath());

		IntellijUtil.configExcludeFolder(project, homeDir);
	}

}