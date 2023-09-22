/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.server.LiferayServerConfigurationProducer;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ProjectConfigurationUtil;

import java.util.List;

/**
 * @author Joye Luo
 * @author Simon Jiang
 */
public class InitBundleMavenAction extends AbstractLiferayMavenGoalAction implements LiferayWorkspaceSupport {

	public InitBundleMavenAction() {
		super("InitBundle", "Run initBundle goal", LiferayIcons.LIFERAY_ICON);

		goals = List.of("bundle-support:init");
	}

	@Override
	protected void handleProcessTerminated(Project project, int exitCode) {
		super.handleProcessTerminated(project, exitCode);

		if (exitCode != 0) {
			return;
		}

		String homeDir = getMavenProperty(
			project, WorkspaceConstants.MAVEN_HOME_DIR_PROPERTY, WorkspaceConstants.HOME_DIR_DEFAULT);

		ProjectConfigurationUtil.configExcludedFolder(project, homeDir);

		ProjectConfigurationUtil.handleServerConfiguration(
			project, LiferayServerConfigurationProducer.getProducers(project));
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		if (super.isEnabledAndVisible(anActionEvent)) {
			VirtualFile rootDir = getVirtualFile(anActionEvent);

			if ((rootDir != null) &&
				rootDir.equals(LiferayWorkspaceSupport.getWorkspaceVirtualFile(anActionEvent.getProject()))) {

				return true;
			}

			return false;
		}

		return false;
	}

}