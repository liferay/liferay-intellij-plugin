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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.server.LiferayServerConfigurationProducer;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ProjectConfigurationUtil;

/**
 * @author Andy Wu
 * @author Simon Jiang
 */
public class InitBundleGradleAction extends AbstractLiferayGradleTaskAction implements LiferayWorkspaceSupport {

	public InitBundleGradleAction() {
		super("InitBundle", "Run initBundle task", LiferayIcons.LIFERAY_ICON, "initBundle");
	}

	@Override
	protected void afterTask(Project project) {
		ProjectConfigurationUtil.handleServerConfiguration(
			project, LiferayServerConfigurationProducer.getProducers(project));
	}

	@Override
	protected void handleProcessTerminated(Project project) {
		super.handleProcessTerminated(project);

		ProjectConfigurationUtil.configExcludedFolder(project, getHomeDir(project));
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		if (super.isEnabledAndVisible(anActionEvent)) {
			VirtualFile baseDir = LiferayWorkspaceSupport.getWorkspaceVirtualFile(anActionEvent.getProject());

			if (baseDir == null) {
				return false;
			}

			if (baseDir.equals(getVirtualFile(anActionEvent))) {
				return true;
			}
		}

		return false;
	}

}