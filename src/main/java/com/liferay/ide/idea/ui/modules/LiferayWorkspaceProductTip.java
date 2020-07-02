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

		setTitle("New Liferay Module Project");
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
		setOKButtonText("Update product setting...");

		return super.getOKAction();
	}

	private Project _project;

}