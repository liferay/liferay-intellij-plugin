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