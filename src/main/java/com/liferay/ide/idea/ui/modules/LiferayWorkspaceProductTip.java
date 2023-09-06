/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.ui.components.JBLabel;

import com.liferay.ide.idea.core.MessagesBundle;

import java.awt.GridLayout;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ethan Sun
 */
public class LiferayWorkspaceProductTip extends DialogWrapper {

	public LiferayWorkspaceProductTip(@Nullable Project project) {
		super(true);

		_project = project;

		init();

		setTitle("Update Liferay Workspace Product Setting");
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		JPanel tipPanel = new JPanel(new GridLayout());

		String warningText = MessagesBundle.message("modules.workspace.product", _project.getName());

		JBLabel msg = new JBLabel(warningText, Messages.getQuestionIcon(), JLabel.CENTER);

		tipPanel.add(msg);

		return tipPanel;
	}

	@Override
	protected void doOKAction() {
		super.doOKAction();

		Application application = ApplicationManager.getApplication();

		application.invokeAndWait(
			() -> {
				LiferayWorkspaceProductDialog liferayWorkspaceProductDialog = new LiferayWorkspaceProductDialog(
					_project);

				if (liferayWorkspaceProductDialog.showAndGet()) {
					ProjectRootManager projectRootManager = ProjectRootManager.getInstance(_project);

					VfsUtil.markDirtyAndRefresh(false, true, true, projectRootManager.getContentRoots());
				}
			});
	}

	@NotNull
	@Override
	protected Action getCancelAction() {
		setCancelButtonText("Ignore");

		return super.getCancelAction();
	}

	@NotNull
	@Override
	protected Action getOKAction() {
		setOKButtonText("Update Liferay Workspace product setting...");

		return super.getOKAction();
	}

	private Project _project;

}