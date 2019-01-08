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

package com.liferay.ide.idea.ui.modules.ext;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayBundle;
import com.liferay.ide.idea.ui.compoments.FixedSizeRefreshButton;
import com.liferay.ide.idea.util.GradleDependency;
import com.liferay.ide.idea.util.GradleDependencyUpdater;
import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

import java.io.IOException;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Charle Wu
 */
public class NewModuleExtFilesAction extends AnAction implements DumbAware {

	@Override
	public void actionPerformed(AnActionEvent event) {
		new OverrideFilesDialog(event.getProject()).show();
	}

	@Override
	public void update(AnActionEvent event) {
		Presentation presentation = event.getPresentation();

		presentation.setEnabledAndVisible(false);

		VirtualFile contextFile = event.getData(CommonDataKeys.VIRTUAL_FILE);

		VirtualFile moduleExtDir = LiferayWorkspaceUtil.getModuleExtDirFile(event.getProject());

		if ((contextFile == null) || (moduleExtDir == null)) {
			return;
		}

		String contextUrl = contextFile.getUrl();

		if (contextFile.equals(moduleExtDir) || !contextUrl.startsWith(moduleExtDir.getUrl())) {
			return;
		}

		Module module = event.getData(LangDataKeys.MODULE);

		if (module == null) {
			return;
		}

		ModuleRootManager rootManager = ModuleRootManager.getInstance(module);

		for (VirtualFile contentRoot : rootManager.getContentRoots()) {
			for (VirtualFile child : contentRoot.getChildren()) {
				if ("build.gradle".equals(child.getName()) && child.exists()) {
					_gradleFile = child;
					presentation.setEnabledAndVisible(true);

					return;
				}
			}
		}

		_gradleFile = null;
	}

	private static final Logger _log = Logger.getInstance(NewModuleExtFilesAction.class);

	private VirtualFile _gradleFile;

	private class OverrideFilesDialog extends DialogWrapper {

		public OverrideFilesDialog(final Project project) {
			super(project, true);

			_project = project;
			init();
			setTitle(LiferayBundle.message("modules.ext.files.title"));
		}

		@Override
		public void show() {
			Application application = ApplicationManager.getApplication();

			application.executeOnPooledThread(this::_setOriginalModuleText);

			_overrideFilesPanel.function = () -> _libraryData;
			_overrideFilesPanel.prepareRefreshButton(_refreshButton, this::_setOriginalModuleText);
			_overrideFilesPanel.setProject(_project);

			super.show();
		}

		@Override
		protected JComponent createCenterPanel() {
			return _topPanel;
		}

		@Override
		protected void doOKAction() {
			_overrideFilesPanel.doFinish(_gradleFile.getParent());
			super.doOKAction();
		}

		private void _getLibraryData() {
			try {
				List<LibraryData> targetPlatformArtifacts = LiferayWorkspaceUtil.getTargetPlatformArtifacts(_project);

				if (targetPlatformArtifacts.isEmpty()) {
					return;
				}

				GradleDependencyUpdater updater = new GradleDependencyUpdater(VfsUtil.virtualToIoFile(_gradleFile));

				List<GradleDependency> originalModules = updater.getDependenciesByName("originalModule");

				if (!originalModules.isEmpty()) {
					GradleDependency dependency = originalModules.get(0);

					String artifact = dependency.getName();

					for (LibraryData lib : targetPlatformArtifacts) {
						if (artifact.equals(lib.getArtifactId())) {
							_libraryData = lib;

							break;
						}
					}
				}
			}
			catch (IOException ioe) {
				_log.error(ioe);
			}
		}

		private void _setOriginalModuleText() {
			_getLibraryData();

			if (_libraryData != null) {
				_originalModuleText.setText(
					_libraryData.getGroupId() + ":" + _libraryData.getArtifactId() + ":" + _libraryData.getVersion());
			}
		}

		private LibraryData _libraryData;
		private JTextField _originalModuleText;
		private OverrideFilesComponent _overrideFilesPanel;
		private final Project _project;
		private FixedSizeRefreshButton _refreshButton;
		private JPanel _topPanel;

	}

}