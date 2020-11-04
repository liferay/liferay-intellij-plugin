// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.liferay.ide.idea.util;

import com.intellij.ide.highlighter.ProjectFileType;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.components.impl.stores.IProjectStore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.project.ProjectKt;

import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

public class ProjectUtil {

	public static boolean isSameProject(@NotNull Path projectFile, @NotNull Project project) {
		IProjectStore projectStore = ProjectKt.getStateStore(project);
		Path existingBaseDirPath = projectStore.getProjectBasePath();

		if (existingBaseDirPath.getFileSystem() != projectFile.getFileSystem()) {
			return false;
		}

		if (Files.isDirectory(projectFile)) {
			return FileUtil.pathsEqual(projectFile.toString(), existingBaseDirPath.toString());
		}

		if (projectStore.getStorageScheme() == StorageScheme.DEFAULT) {
			return FileUtil.pathsEqual(FileUtil.toSystemIndependentName(projectFile.toString()), projectStore.getProjectFilePath());
		}

		Path parent = projectFile.getParent();
		if (parent == null) {
			return false;
		}

		Path parentFileName = parent.getFileName();
		if (parentFileName != null && parentFileName.toString().equals(Project.DIRECTORY_STORE_FOLDER)) {
			parent = parent.getParent();
			return parent != null && FileUtil.pathsEqual(parent.toString(), existingBaseDirPath.toString());
		}

		return projectFile.getFileName().toString().endsWith(ProjectFileType.DOT_DEFAULT_EXTENSION) &&
				FileUtil.pathsEqual(parent.toString(), existingBaseDirPath.toString());
	}

}