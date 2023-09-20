/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.server.LiferayServerConfigurationProducer;
import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.FileUtil;
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
	protected void handleProcessTerminated(Project project) {
		super.handleProcessTerminated(project);

		String homeDir = getMavenProperty(
			project, WorkspaceConstants.MAVEN_HOME_DIR_PROPERTY, WorkspaceConstants.HOME_DIR_DEFAULT);

		ProjectConfigurationUtil.configExcludedFolder(project, homeDir);

		try {
			if (CoreUtil.isNullOrEmpty(homeDir)) {
				return;
			}

			VirtualFile projectDir = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

			if (projectDir == null) {
				return;
			}

			VirtualFile liferayHomeDirVirtualFile = projectDir.findChild(homeDir);

			if (liferayHomeDirVirtualFile == null) {
				return;
			}

			if (FileUtil.notExists(liferayHomeDirVirtualFile.toNioPath())) {
				return;
			}

			ProjectConfigurationUtil.handleServerConfiguration(
				project, LiferayServerConfigurationProducer.getProducers(project));
		}
		catch (Exception exception) {
			_logger.error(exception);
		}
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

	private static final Logger _logger = Logger.getInstance(InitBundleMavenAction.class);

}