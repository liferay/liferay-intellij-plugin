/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules.ext;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
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

import org.jetbrains.annotations.NotNull;

/**
 * @author Charles Wu
 */
public class NewModuleExtFilesAction extends AnAction implements DumbAware {

	public NewModuleExtFilesAction() {
		super(LiferayIcons.LIFERAY_ICON);
	}

	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		OverrideFilesDialog overrideFilesDialog = new OverrideFilesDialog(anActionEvent.getProject());

		overrideFilesDialog.show();
	}

	@NotNull
	@Override
	public ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

	@Override
	public void update(AnActionEvent anActionEvent) {
		Presentation presentation = anActionEvent.getPresentation();

		presentation.setEnabledAndVisible(false);

		Project project = anActionEvent.getProject();

		if (Objects.isNull(project)) {
			return;
		}

		VirtualFile contextVirtualFile = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);

		VirtualFile moduleExtDir = LiferayWorkspaceSupport.getModuleExtDirFile(project);

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
				if (Objects.equals(child.getName(), "build.gradle") && child.exists()) {
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
			catch (IOException ioException) {
				_log.error(ioException);
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