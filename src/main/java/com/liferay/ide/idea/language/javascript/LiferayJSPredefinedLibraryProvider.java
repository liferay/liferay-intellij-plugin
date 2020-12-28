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
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.webcore.libraries.ScriptingLibraryManager;
import com.intellij.webcore.libraries.ScriptingLibraryModel;

import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.IntellijUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
public class LiferayJSPredefinedLibraryProvider extends JSPredefinedLibraryProvider implements ModuleRootListener {

	@NotNull
	@Override
	public ScriptingLibraryModel[] getPredefinedLibraries(@NotNull Project project) {
		ScriptingLibraryModel scriptingLibraryModel = ScriptingLibraryModel.createPredefinedLibrary(
			_LIFERAY_JAVASCRIPT_LIBRARY_NAME, VfsUtilCore.toVirtualFileArray(_getJavascriptFiles(project)), true);

		if (!_moduleRootListenerRegistered) {
			MessageBus messageBus = project.getMessageBus();

			MessageBusConnection messageBusConnection = messageBus.connect(project);

			messageBusConnection.subscribe(ProjectTopics.PROJECT_ROOTS, this);

			_moduleRootListenerRegistered = true;
		}

		return new ScriptingLibraryModel[] {scriptingLibraryModel};
	}

	@Override
	public void rootsChanged(ModuleRootEvent event) {
		Object source = event.getSource();

		if (!(source instanceof Project)) {
			return;
		}

		Project project = (Project)event.getSource();

		DumbService dumbService = DumbService.getInstance(project);

		dumbService.smartInvokeLater(
			() -> {
				ScriptingLibraryManager scriptingLibraryManager = JSLibraryManager.getInstance(project);

				ScriptingLibraryModel liferayScriptingLibraryModel = scriptingLibraryManager.getLibraryByName(
					_LIFERAY_JAVASCRIPT_LIBRARY_NAME);

				if (liferayScriptingLibraryModel != null) {
					Set<VirtualFile> oldSourceFiles = liferayScriptingLibraryModel.getSourceFiles();
					Set<VirtualFile> javascriptFiles = _getJavascriptFiles(project);

					boolean filesChanged = true;

					if ((oldSourceFiles.size() == javascriptFiles.size()) &&
						oldSourceFiles.containsAll(javascriptFiles)) {

						filesChanged = false;
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

	@NotNull
	private static Set<VirtualFile> _getJavascriptFiles(@NotNull Project project) {
		List<LibraryData> targetPlatformArtifacts = _getTargetPlatformArtifacts(project);

		Stream<LibraryData> stream = targetPlatformArtifacts.stream();

		return stream.filter(
			libraryData -> Objects.equals("com.liferay", libraryData.getGroupId())
		).filter(
			libraryData ->
				Objects.equals("com.liferay.frontend.js.web", libraryData.getArtifactId()) ||
				Objects.equals("com.liferay.frontend.js.aui.web", libraryData.getArtifactId())
		).map(
			LiferayJSPredefinedLibraryProvider::_getJavascriptFilesFromLibraryData
		).flatMap(
			javascriptFiles -> javascriptFiles.stream()
		).collect(
			Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(VirtualFile::getUrl)))
		);
	}

	@NotNull
	@SuppressWarnings("rawtypes")
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

					if (Objects.equals("js", virtualFile.getExtension())) {
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
				VirtualFile jarRoot = IntellijUtil.getJarRoot(rootVirtualFile);

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

		return GradleUtil.getTargetPlatformArtifacts(project);
	}

	private static final String _LIFERAY_JAVASCRIPT_LIBRARY_NAME = "Liferay JavaScript";

	private static boolean _moduleRootListenerRegistered = false;
	private static List<LibraryData> _targetPlatformArtifacts = new ArrayList<>();

}