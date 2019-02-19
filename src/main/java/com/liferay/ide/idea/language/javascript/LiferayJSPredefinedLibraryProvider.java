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

package com.liferay.ide.idea.language.javascript;

import com.intellij.lang.javascript.library.JSPredefinedLibraryProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.webcore.libraries.ScriptingLibraryModel;

import java.net.URL;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * Provide predefined JavaScript Libraries for Liferay
 *
 * @author Dominik Marks
 */
public class LiferayJSPredefinedLibraryProvider extends JSPredefinedLibraryProvider {

	@NotNull
	@Override
	public ScriptingLibraryModel[] getPredefinedLibraries(@NotNull Project project) {
		Set<VirtualFile> javascriptFiles = _getJavascriptFiles();

		VirtualFile[] sourceFiles = javascriptFiles.toArray(new VirtualFile[0]);

		ScriptingLibraryModel scriptingLibraryModel = ScriptingLibraryModel.createPredefinedLibrary(
			_LIFERAY_JAVASCRIPT_LIBRARY_DESCRIPTIVE_NAME, sourceFiles, true);

		return new ScriptingLibraryModel[] {scriptingLibraryModel};
	}

	@NotNull
	private static Set<VirtualFile> _getJavascriptFiles() {
		Set<VirtualFile> virtualFiles = new HashSet<>();

		virtualFiles.addAll(_getJavascriptFilesFromResource("definitions/js/aui"));
		virtualFiles.addAll(_getJavascriptFilesFromResource("definitions/js/frontend"));

		return virtualFiles;
	}

	@NotNull
	private static Set<VirtualFile> _getJavascriptFilesFromDirectory(@NotNull VirtualFile directory) {
		Set<VirtualFile> virtualFiles = new HashSet<>();

		VfsUtilCore.visitChildrenRecursively(
			directory,
			new VirtualFileVisitor() {

				@Override
				public boolean visitFile(@NotNull VirtualFile virtualFile) {
					if (virtualFile.isDirectory()) {
						return true;
					}

					virtualFiles.add(virtualFile);

					return true;
				}

			});

		return virtualFiles;
	}

	@NotNull
	private static Set<VirtualFile> _getJavascriptFilesFromResource(@NotNull String resource) {
		Set<VirtualFile> virtualFiles = new HashSet<>();

		ClassLoader classLoader = LiferayJSPredefinedLibraryProvider.class.getClassLoader();

		URL resourceURL = classLoader.getResource(resource);

		if (resourceURL != null) {
			VirtualFile resourceDirectory = VfsUtil.findFileByURL(resourceURL);

			if (resourceDirectory != null) {
				virtualFiles.addAll(_getJavascriptFilesFromDirectory(resourceDirectory));
			}
		}

		return virtualFiles;
	}

	private static final String _LIFERAY_JAVASCRIPT_LIBRARY_DESCRIPTIVE_NAME = "Liferay JavaScripts";

}