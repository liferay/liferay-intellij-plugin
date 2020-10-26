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

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.core.MessagesBundle;
import com.liferay.ide.idea.ui.compoments.FixedSizeRefreshButton;
import com.liferay.ide.idea.util.GradleDependency;
import com.liferay.ide.idea.util.GradleDependencyUpdater;
import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.io.IOException;

import java.util.List;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Charles Wu
 */
public class NewModuleExtFilesAction extends AnAction implements DumbAware, LiferayWorkspaceSupport {

	public NewModuleExtFilesAction() {
		super(LiferayIcons.LIFERAY_ICON);
	}

	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		OverrideFilesDialog overrideFilesDialog = new OverrideFilesDialog(anActionEvent.getProject());

		overrideFilesDialog.show();
	}

	@Override
	public void update(AnActionEvent anActionEvent) {
		Presentation presentation = anActionEvent.getPresentation();

		presentation.setEnabledAndVisible(false);

		VirtualFile contextVirtualFile = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);

		VirtualFile moduleExtDir = getModuleExtDirFile(anActionEvent.getProject());

		if ((contextVirtualFile == null) || (moduleExtDir == null)) {
			return;
		}

		String contextUrl = contextVirtualFile.getUrl();

		if (contextVirtualFile.equals(moduleExtDir) || !contextUrl.startsWith(moduleExtDir.getUrl())) {
			return;
		}

		Module module = anActionEvent.getData(LangDataKeys.MODULE);

		if (module == null) {
			return;
		}

		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		for (VirtualFile contentRoot : moduleRootManager.getContentRoots()) {
			for (VirtualFile child : contentRoot.getChildren()) {
				if (Objects.equals("build.gradle", child.getName()) && child.exists()) {
					_gradleVirtualFile = child;
					presentation.setEnabledAndVisible(true);

					return;
				}
			}
		}

		_gradleVirtualFile = null;
	}

	private static final Logger _log = Logger.getInstance(NewModuleExtFilesAction.class);

	private VirtualFile _gradleVirtualFile;

	private class OverrideFilesDialog extends DialogWrapper {

		public OverrideFilesDialog(final Project project) {
			super(project, true);

			_project = project;
			init();
			setTitle(MessagesBundle.message("modules.ext.files.title"));
		}

		@Override
		public void show() {
			Application application = ApplicationManager.getApplication();

			application.executeOnPooledThread(this::_setOriginalModuleText);

			_overrideFilesPanel.function = () -> _libraryData;
			_overrideFilesPanel.prepareRefreshButton(_refreshButton, true, this::_setOriginalModuleText);
			_overrideFilesPanel.setProject(_project);

			super.show();
		}

		@Override
		protected JComponent createCenterPanel() {
			return _topPanel;
		}

		@Override
		protected void doOKAction() {
			_overrideFilesPanel.doFinish(_gradleVirtualFile.getParent());
			super.doOKAction();
		}

		private void _getLibraryData() {
			try {
				List<LibraryData> targetPlatformArtifacts = GradleUtil.getTargetPlatformArtifacts(_project);

				if (targetPlatformArtifacts.isEmpty()) {
					return;
				}

				GradleDependencyUpdater gradleDependencyUpdater = new GradleDependencyUpdater(
					VfsUtil.virtualToIoFile(_gradleVirtualFile));

				List<GradleDependency> originalModules = gradleDependencyUpdater.getDependenciesByName(
					"originalModule");

				if (!originalModules.isEmpty()) {
					GradleDependency gradleDependency = originalModules.get(0);

					String artifact = gradleDependency.getName();

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
				_originalModuleTextField.setText(
					_libraryData.getGroupId() + ":" + _libraryData.getArtifactId() + ":" + _libraryData.getVersion());
			}
		}

		private LibraryData _libraryData;
		private JTextField _originalModuleTextField;
		private OverrideFilesComponent _overrideFilesPanel;
		private final Project _project;
		private FixedSizeRefreshButton _refreshButton;
		private JPanel _topPanel;

	}

}