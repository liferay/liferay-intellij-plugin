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

import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.externalSystem.model.project.LibraryPathType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.GradleDependency;
import com.liferay.ide.idea.util.GradleDependencyUpdater;
import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import org.jetbrains.annotations.NotNull;

/**
 * @author Ethan Sun
 */
public class CompareOriginalImplementationAction extends AnAction implements LiferayWorkspaceSupport {

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		DiffRequestFactory diffRequestFactory = DiffRequestFactory.getInstance();

		DiffRequest request = diffRequestFactory.createFromFiles(_project, _selectedFile, _targetFile);

		request.putUserData(DiffUserDataKeys.HELP_ID, null);

		DiffManager diffManager = DiffManager.getInstance();

		diffManager.showDiff(_project, request);
	}

	@Override
	public boolean isDumbAware() {
		return false;
	}

	@Override
	public void update(AnActionEvent anActionEvent) {
		_project = anActionEvent.getProject();

		Presentation presentation = anActionEvent.getPresentation();

		presentation.setEnabledAndVisible(false);

		VirtualFile contextVirtualFile = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);

		VirtualFile moduleExtDir = getModuleExtDirFile(_project);

		if ((contextVirtualFile == null) || (moduleExtDir == null)) {
			return;
		}

		String contextUrl = contextVirtualFile.getUrl();

		if (contextVirtualFile.isDirectory() || !contextUrl.startsWith(moduleExtDir.getUrl()) ||
			!contextUrl.contains("/src/main/")) {

			return;
		}

		DataContext context = anActionEvent.getDataContext();

		_selectedFile = CommonDataKeys.VIRTUAL_FILE.getData(context);

		if ((_selectedFile == null) && !_selectedFile.exists()) {
			return;
		}

		Module module = anActionEvent.getData(LangDataKeys.MODULE);

		if (module == null) {
			return;
		}

		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		VirtualFile moduleRootManagerContentRoot = moduleRootManager.getContentRoots()[0];

		VirtualFile moduleRootManagerContentRootParent = moduleRootManagerContentRoot.getParent();

		VirtualFile extModuleVirtualFile = moduleRootManagerContentRootParent.getParent();

		for (VirtualFile child : extModuleVirtualFile.getChildren()) {
			if (Objects.equals("build.gradle", child.getName()) && child.exists()) {
				File sourceJar = _getSourceJar(child);

				if (Objects.isNull(sourceJar)) {
					return;
				}

				_targetFile = _searchTargetFile(_selectedFile, sourceJar);

				if (Objects.isNull(_targetFile)) {
					return;
				}

				if ((_targetFile != null) && _targetFile.exists()) {
					presentation.setEnabledAndVisible(true);
				}

				break;
			}
		}
	}

	private File _getSourceJar(VirtualFile virtualFile) {
		List<LibraryData> targetPlatformArtifacts = GradleUtil.getTargetPlatformArtifacts(_project);

		if (!targetPlatformArtifacts.isEmpty() && LiferayWorkspaceSupport.isValidGradleWorkspaceProject(_project) &&
			virtualFile.exists()) {

			try {
				GradleDependencyUpdater gradleDependencyUpdater = new GradleDependencyUpdater(
					VfsUtil.virtualToIoFile(virtualFile));

				List<GradleDependency> originalModules = gradleDependencyUpdater.getDependenciesByName(
					"originalModule");

				if (!originalModules.isEmpty()) {
					GradleDependency gradleDependency = originalModules.get(0);

					String artifact = gradleDependency.getName();

					for (LibraryData lib : targetPlatformArtifacts) {
						if (artifact.equals(lib.getArtifactId())) {
							Set<String> paths = lib.getPaths(LibraryPathType.SOURCE);

							Iterator<String> iterator = paths.iterator();

							String jarPath = iterator.next();

							return new File(jarPath);
						}
					}
				}
			}
			catch (IOException ioe) {
				_log.error("Can not find original source file for " + _project.getName(), ioe);
			}
		}

		return null;
	}

	private VirtualFile _searchTargetFile(VirtualFile selectedFile, File sourceJar) {
		VirtualFile targetFile = null;

		if (FileUtil.exists(sourceJar)) {
			try (ZipFile zipFile = new ZipFile(sourceJar)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();

				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();

					String entryCanonicalName = entry.getName();

					if (!entry.isDirectory() &&
						StringUtil.contains(
							Objects.requireNonNull(selectedFile.getCanonicalPath()), entryCanonicalName)) {

						InputStream in = zipFile.getInputStream(entry);

						final File tempFile = File.createTempFile(selectedFile.getName(), ".tmp");

						tempFile.deleteOnExit();

						try (FileOutputStream out = new FileOutputStream(tempFile)) {
							IOUtils.copy(in, out);
						}

						targetFile = VfsUtil.findFileByIoFile(tempFile, true);
					}
				}
			}
			catch (IOException ioe) {
				_log.error("Failed to compare with original file for project " + _project.getName(), ioe);
			}
		}

		return targetFile;
	}

	private static final Logger _log = Logger.getInstance(CompareOriginalImplementationAction.class);

	private Project _project;
	private VirtualFile _selectedFile;
	private VirtualFile _targetFile;

}