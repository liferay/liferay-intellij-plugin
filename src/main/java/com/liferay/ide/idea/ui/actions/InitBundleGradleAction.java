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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import icons.LiferayIcons;

/**
 * @author Andy Wu
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
	protected String getWorkingDirectory(AnActionEvent event) {
		Project project = event.getRequiredData(CommonDataKeys.PROJECT);

		return project.getBasePath();
	}

}