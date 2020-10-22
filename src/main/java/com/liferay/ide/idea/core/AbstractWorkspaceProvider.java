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

package com.liferay.ide.idea.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.util.CoreUtil;

import java.io.File;

/**
 * @author Simon Jiang
 */
public abstract class AbstractWorkspaceProvider implements WorkspaceProvider {

	public AbstractWorkspaceProvider() {
	}

	public AbstractWorkspaceProvider(Project project) {
		this.project = project;
	}

	@Override
	public String getLiferayVersion() {
		String targetPlatformVersion = getTargetPlatformVersion();

		if (!CoreUtil.isNullOrEmpty(targetPlatformVersion)) {
			String[] versionArr = targetPlatformVersion.split("\\.");

			return versionArr[0] + "." + versionArr[1];
		}

		return null;
	}

	@Override
	public VirtualFile getModuleExtDirFile() {
		if (project == null) {
			return null;
		}

		String moduleExtDir = getWorkspaceProperty(
			WorkspaceConstants.EXT_DIR_PROPERTY, WorkspaceConstants.EXT_DIR_DEFAULT);

		File file = new File(project.getBasePath(), moduleExtDir);

		if (!file.isAbsolute()) {
			String projectBasePath = project.getBasePath();

			if (projectBasePath == null) {
				return null;
			}

			file = new File(projectBasePath, moduleExtDir);
		}

		LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

		return localFileSystem.findFileByPath(file.getPath());
	}

	protected Project project;

}