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

import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;

import com.liferay.ide.idea.terminal.GogoShellToolWindowFactory;
import com.liferay.ide.idea.terminal.GogoShellView;

import icons.LiferayIcons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 */
public class OpenGogoShellAction extends DumbAwareAction {

	public OpenGogoShellAction() {
		super("Gogo Shell", "Open Gogo Shell Terminal", LiferayIcons.OSGI_ICON);
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		Project project = getEventProject(e);
		VirtualFile selectedFile = _getSelectedFile(e);

		if ((project == null) || (selectedFile == null)) {
			return;
		}

		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

		ToolWindow toolWindow = toolWindowManager.getToolWindow(GogoShellToolWindowFactory.TOOL_WINDOW_ID);

		if ((toolWindow != null) && toolWindow.isAvailable()) {
			GogoShellView googleShellView = GogoShellView.getInstance(project);

			googleShellView.openLocalSession(project, toolWindow);
		}
	}

	@Override
	public void update(AnActionEvent event) {
		Project project = getEventProject(event);

		Presentation presentation = event.getPresentation();

		presentation.setEnabledAndVisible((project != null) && (_getSelectedFile(event) != null));
	}

	@Nullable
	private static VirtualFile _getSelectedFile(@NotNull AnActionEvent e) {
		return ShowFilePathAction.findLocalFile(e.getData(CommonDataKeys.VIRTUAL_FILE));
	}

}