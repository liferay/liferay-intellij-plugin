/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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
			String[] version = targetPlatformVersion.split("\\.");

			return version[0] + "." + version[1];
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

		File extDir = new File(project.getBasePath(), moduleExtDir);

		if (!extDir.isAbsolute()) {
			String projectBasePath = project.getBasePath();

			if (projectBasePath == null) {
				return null;
			}

			extDir = new File(projectBasePath, moduleExtDir);
		}

		LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

		return localFileSystem.findFileByPath(extDir.getPath());
	}

	protected Project project;

}