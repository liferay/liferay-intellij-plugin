/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

/**
 * @author Simon Jiang
 */
public interface WorkspaceProvider {

	public default boolean getIndexSources() {
		return false;
	}

	public String getLiferayHome();

	public String getLiferayVersion();

	public VirtualFile getModuleExtDirFile();

	public default List<String> getTargetPlatformDependencies() {
		return null;
	}

	public String getTargetPlatformVersion();

	public String[] getWorkspaceModuleDirs();

	public default ProductInfo getWorkspaceProductInfo() {
		return null;
	}

	public String getWorkspaceProperty(String key, String defaultValue);

	public String[] getWorkspaceWarDirs();

	public boolean isFlexibleLiferayWorkspace();

	public default boolean isGradleWorkspace() {
		return false;
	}

	public <T> T provide(Project project, Class<T> adapterType);

}