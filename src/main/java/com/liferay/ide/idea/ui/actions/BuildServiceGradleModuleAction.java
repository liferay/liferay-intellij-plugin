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
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Ethan Sun
 */
public class BuildServiceGradleModuleAction extends AbstractLiferayGradleTaskAction implements LiferayWorkspaceSupport {

	public BuildServiceGradleModuleAction() {
		super("BuildService", "Run buildService task", LiferayIcons.LIFERAY_ICON, "buildService");
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		Project project = anActionEvent.getProject();

		if (LiferayWorkspaceSupport.isValidGradleWorkspaceProject(project)) {
			VirtualFile virtualFile = getVirtualFile(anActionEvent);

			if ((project == null) || (virtualFile == null)) {
				return false;
			}

			Module module = ModuleUtil.findModuleForFile(virtualFile, project);

			if (module == null) {
				return false;
			}

			return !Objects.isNull(virtualFile.findChild("service.xml"));
		}

		return false;
	}

}