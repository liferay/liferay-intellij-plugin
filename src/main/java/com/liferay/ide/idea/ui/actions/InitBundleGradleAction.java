/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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
	protected void handleProcessTerminated(Project project, int exitCode) {
		super.handleProcessTerminated(project, exitCode);

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