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

import com.intellij.ProjectTopics;
import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.lang.javascript.library.JSPredefinedLibraryProvider;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.externalSystem.model.project.LibraryPathType;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.webcore.libraries.ScriptingLibraryManager;
import com.intellij.webcore.libraries.ScriptingLibraryModel;

import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		Set<VirtualFile> javascriptFiles = _getJavascriptFiles(project);

		VirtualFile[] javascriptFilesArray = VfsUtilCore.toVirtualFileArray(javascriptFiles);

		ScriptingLibraryModel scriptingLibraryModel = ScriptingLibraryModel.createPredefinedLibrary(
			_LIFERAY_JAVASCRIPT_LIBRARY_DESCRIPTIVE_NAME, javascriptFilesArray, true);

		if (!_rootsChangeListenerRegistered) {
			MessageBus messageBus = project.getMessageBus();

			MessageBusConnection messageBusConnection = messageBus.connect(project);

			messageBusConnection.subscribe(
				ProjectTopics.PROJECT_ROOTS,
				new ModuleRootListener() {

					@Override
					public void rootsChanged(@NotNull ModuleRootEvent moduleRootEvent) {
						DumbService dumbService = DumbService.getInstance(project);

						dumbService.smartInvokeLater(
							() -> {
								ScriptingLibraryManager scriptingLibraryManager = JSLibraryManager.getInstance(project);

								ScriptingLibraryModel liferayScriptingLibraryModel =
									scriptingLibraryManager.getLibraryByName(
										_LIFERAY_JAVASCRIPT_LIBRARY_DESCRIPTIVE_NAME);

								if (liferayScriptingLibraryModel != null) {
									Set<VirtualFile> oldSourceFiles = liferayScriptingLibraryModel.getSourceFiles();
									Set<VirtualFile> javascriptFiles = _getJavascriptFiles(project);

									boolean filesChanged = true;

									if (oldSourceFiles.size() == javascriptFiles.size()) {
										if (oldSourceFiles.containsAll(javascriptFiles)) {
											filesChanged = false;
										}
									}

									if (filesChanged) {
										WriteAction.run(
											() -> {
												liferayScriptingLibraryModel.setSourceFiles(
													VfsUtilCore.toVirtualFileArray(javascriptFiles));
												scriptingLibraryManager.commitChanges();
											});
									}
								}
							});
					}

				});

			_rootsChangeListenerRegistered = true;
		}

		return new ScriptingLibraryModel[] {scriptingLibraryModel};
	}

	protected static void setTargetPlatformArtifacts(List<LibraryData> targetPlatformArtifacts) {
		LiferayJSPredefinedLibraryProvider._targetPlatformArtifacts = targetPlatformArtifacts;
	}

	@NotNull
	private static Set<VirtualFile> _getJavascriptFiles(@NotNull Project project) {
		Set<VirtualFile> virtualFiles = new TreeSet<>(Comparator.comparing(VirtualFile::getUrl));

		List<LibraryData> targetPlatformArtifacts = _getTargetPlatformArtifacts(project);

		Stream<LibraryData> targetPlatformArtifactsStream = targetPlatformArtifacts.stream();

		List<LibraryData> javascriptLibrariesDatas = targetPlatformArtifactsStream.filter(
			libraryData -> "com.liferay".equals(libraryData.getGroupId())
		).filter(
			libraryData -> ("com.liferay.frontend.js.web".equals(libraryData.getArtifactId())) ||
			 ("com.liferay.frontend.js.aui.web".equals(libraryData.getArtifactId()))
		).collect(
			Collectors.toList()
		);

		for (LibraryData libraryData : javascriptLibrariesDatas) {
			virtualFiles.addAll(_getJavascriptFilesFromLibraryData(libraryData));
		}

		return virtualFiles;
	}

	@NotNull
	private static Set<VirtualFile> _getJavascriptFilesFromJarRoot(@NotNull VirtualFile jarRoot) {
		Set<VirtualFile> virtualFiles = new HashSet<>();

		VfsUtilCore.visitChildrenRecursively(
			jarRoot,
			new VirtualFileVisitor() {

				@Override
				public boolean visitFile(@NotNull VirtualFile virtualFile) {
					if (virtualFile.isDirectory()) {
						return true;
					}

					String extension = virtualFile.getExtension();

					if ("js".equals(extension)) {
						virtualFiles.add(virtualFile);
					}

					return true;
				}

			});

		return virtualFiles;
	}

	private static Set<VirtualFile> _getJavascriptFilesFromLibraryData(@NotNull LibraryData libraryData) {
		Set<VirtualFile> virtualFiles = new HashSet<>();

		Set<String> sourcePaths = libraryData.getPaths(LibraryPathType.SOURCE);

		String sourcePath = ContainerUtil.getFirstItem(sourcePaths);

		if (sourcePath != null) {
			LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

			VirtualFile rootVirtualFile = localFileSystem.findFileByPath(sourcePath);

			if (rootVirtualFile != null) {
				VirtualFileSystem virtualFileSystem = rootVirtualFile.getFileSystem();

				VirtualFile jarRoot;

				if (virtualFileSystem instanceof JarFileSystem) {
					JarFileSystem jarFileSystem = (JarFileSystem)virtualFileSystem;

					jarRoot = jarFileSystem.getRootByEntry(rootVirtualFile);
				}
				else {
					JarFileSystem jarFileSystem = JarFileSystem.getInstance();

					jarRoot = jarFileSystem.getJarRootForLocalFile(rootVirtualFile);
				}

				if (jarRoot != null) {
					virtualFiles.addAll(_getJavascriptFilesFromJarRoot(jarRoot));
				}
			}
		}

		return virtualFiles;
	}

	@NotNull
	private static List<LibraryData> _getTargetPlatformArtifacts(@NotNull Project project) {
		Application application = ApplicationManager.getApplication();

		if (application.isUnitTestMode()) {
			return _targetPlatformArtifacts;
		}

		return LiferayWorkspaceUtil.getTargetPlatformArtifacts(project);
	}

	private static final String _LIFERAY_JAVASCRIPT_LIBRARY_DESCRIPTIVE_NAME = "Liferay JavaScripts";

	private static boolean _rootsChangeListenerRegistered = false;
	private static List<LibraryData> _targetPlatformArtifacts = new ArrayList<>();

}