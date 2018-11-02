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

package com.liferay.ide.idea.util;

import static com.intellij.openapi.roots.ModuleRootModificationUtil.updateExcludedFolders;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Simon Jiang
 */
public class IntellijUtil {

	public static void configExcludeFolder(@NotNull Project project, String directory) {
		if (!LiferayWorkspaceUtil.isValidWorkspaceLocation(project)) {
			return;
		}

		if (CoreUtil.isNullOrEmpty(directory)) {
			return;
		}

		VirtualFile projectDir = project.getBaseDir();

		projectDir.refresh(
			true, true,
			() -> {
				VirtualFile directoryToExclude = projectDir.findChild(directory);

				if (FileUtil.notExists(directoryToExclude)) {
					return;
				}

				Module module = ModuleUtil.findModuleForFile(projectDir, project);

				if (module == null) {
					return;
				}

				VirtualFile contentRoot = _getContentRoot(module, directoryToExclude);

				if (contentRoot == null) {
					return;
				}

				Collection<String> oldExcludedFolders = _getOldExcludedFolders(module, directoryToExclude);

				if ((oldExcludedFolders.size() == 1) && oldExcludedFolders.contains(directoryToExclude.getUrl())) {
					return;
				}

				updateExcludedFolders(
					module, projectDir, Collections.emptyList(), ContainerUtil.newHashSet(directoryToExclude.getUrl()));
			});
	}

	private static VirtualFile _getContentRoot(@NotNull Module module, @Nullable VirtualFile root) {
		if (root == null) {
			return null;
		}

		ProjectRootManager projectRootManager = ProjectRootManager.getInstance(module.getProject());

		if (projectRootManager == null) {
			return null;
		}

		ProjectFileIndex fileIndex = projectRootManager.getFileIndex();

		return fileIndex.getContentRootForFile(root);
	}

	private static Collection<String> _getOldExcludedFolders(
		@NotNull Module module, @NotNull VirtualFile directoryToExclude) {

		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		return ContainerUtil.filter(
			moduleRootManager.getExcludeRootUrls(), url -> url.startsWith(directoryToExclude.getUrl()));
	}

}