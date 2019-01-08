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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.externalSystem.model.project.LibraryPathType;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.ListUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;

import com.liferay.ide.idea.core.LiferayBundle;
import com.liferay.ide.idea.ui.compoments.FixedSizeRefreshButton;
import com.liferay.ide.idea.util.LiferayWorkspaceUtil;
import com.liferay.ide.idea.util.ZipUtil;

import java.awt.Component;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Charle Wu
 */
public class OverrideFilesComponent {

	public OverrideFilesComponent() {
		listModel = new DefaultListModel<>();

		_jbList = new JBList<>(listModel);

		_prepareListPanel();
	}

	public void doFinish(VirtualFile moduleRoot) {
		Application application = ApplicationManager.getApplication();

		application.executeOnPooledThread(
			() -> {
				Path sourceFolder = Paths.get(moduleRoot.getPath(), "src/main/java/");
				Path resourcesFolder = Paths.get(moduleRoot.getPath(), "src/main/resources/");

				List<String> relativePaths = new ArrayList<>(listModel.getSize());
				Enumeration<EntryDescription> enumeration = listModel.elements();

				while (enumeration.hasMoreElements()) {
					EntryDescription entry = enumeration.nextElement();

					relativePaths.add(entry.getPresentableUrl());
				}

				try {
					ZipUtil.unzip(
						new File(_sourceJar), sourceFolder.toFile(),
						path -> {
							//choose the folder where the file should go

							if (relativePaths.contains(path)) {
								if (path.startsWith("com/")) {
									return Pair.create(true, sourceFolder.toFile());
								}
								else {
									return Pair.create(true, resourcesFolder.toFile());
								}
							}
							else {
								return Pair.create(false, null);
							}
						});
				}
				catch (IOException ioe) {
					_log.error(ioe);
				}

				moduleRoot.refresh(true, true);
			});
	}

	public void prepareRefreshButton(@NotNull FixedSizeRefreshButton refreshButton, Runnable callBack) {
		refreshButton.addActionListener(
			event -> {
				refreshButton.setEnabled(false);

				if (LiferayWorkspaceUtil.getTargetPlatformVersion(_project) == null) {
					Messages.showMessageDialog(
						_project, LiferayBundle.message("modules.ext.targetPlatform.mention"), "Warning",
						Messages.getWarningIcon());
					refreshButton.setEnabled(true);

					return;
				}

				ImportSpecBuilder builder = new ImportSpecBuilder(_project, GradleConstants.SYSTEM_ID);

				builder.use(ProgressExecutionMode.START_IN_FOREGROUND_ASYNC);
				builder.callback(
					new ExternalProjectRefreshCallback() {

						@Override
						public void onFailure(@NotNull String errorMessage, @Nullable String errorDetails) {
							refreshButton.setEnabled(true);
						}

						@Override
						public void onSuccess(@Nullable DataNode<ProjectData> externalProject) {
							Application application = ApplicationManager.getApplication();

							application.invokeLater(callBack);

							refreshButton.setEnabled(true);
						}

					});

				ExternalSystemUtil.refreshProjects(builder);
			});
	}

	public void setProject(@NotNull Project project) {
		_project = project;
	}

	public Function function;
	public DefaultListModel<EntryDescription> listModel;

	@FunctionalInterface
	public interface Function {

		/**
		 * @return the original module LibraryData
		 * @throws ConfigurationException throw a ConfigurationException to display as a dialog
		 */
		public LibraryData get() throws ConfigurationException;

	}

	private void _doAdd() {
		try {
			_sourceJar = _getSourceJar();
		}
		catch (ConfigurationException ce) {
			Messages.showMessageDialog(_project, ce.getMessage(), ce.getTitle(), Messages.getErrorIcon());

			return;
		}

		LocalFileSystem fileSystem = LocalFileSystem.getInstance();

		VirtualFile rootFile = fileSystem.findFileByPath(_sourceJar);

		FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, false, false, true, true);

		descriptor.setTitle(LiferayBundle.message("modules.ext.override.dialog.title"));
		descriptor.setRoots(rootFile);
		descriptor.setForcedToUseIdeaFileChooser(true);

		FileChooser.chooseFiles(
			descriptor, _project, rootFile,
			chosenFiles -> chosenFiles.forEach(
				file -> {
					EntryDescription description = new EntryDescription(file);

					if (!listModel.contains(description)) {
						listModel.addElement(description);
					}
				}));
	}

	private void _doRemove() {
		int[] selected = _jbList.getSelectedIndices();

		ListUtil.removeIndices(_jbList, selected);
	}

	private String _getSourceJar() throws ConfigurationException {
		LibraryData lib = function.get();

		if (lib != null) {
			return ContainerUtil.getFirstItem(lib.getPaths(LibraryPathType.SOURCE));
		}

		throw new ConfigurationException(LiferayBundle.message("modules.ext.override.jar.undefined"), "Error");
	}

	private void _prepareListPanel() {
		_jbList.setCellRenderer(
			new DefaultListCellRenderer() {

				@Override
				public final Component getListCellRendererComponent(
					JList list, Object value, int index, boolean selected, boolean cellHasFocus) {

					super.getListCellRendererComponent(list, _getItemText(value), index, selected, cellHasFocus);

					if (selected) {
						setForeground(UIUtil.getListSelectionForeground());
					}
					else if (value instanceof EntryDescription && !((EntryDescription)value).isValid()) {
						setForeground(JBColor.RED);
					}

					setIcon(((EntryDescription)value).getIconFor());

					return this;
				}

				private String _getItemText(Object value) {
					if (value instanceof EntryDescription) {
						return ((EntryDescription)value).getPresentableUrl();
					}

					return "UNKNOWN OBJECT";
				}

			});

		ToolbarDecorator decorator = ToolbarDecorator.createDecorator(_jbList);

		decorator.disableUpDownActions();
		decorator.setAddAction(action -> _doAdd());
		decorator.setRemoveAction(action -> _doRemove());

		_sourcePanel.add(decorator.createPanel());
	}

	private static final Logger _log = Logger.getInstance(LiferayModuleExtBuilder.class);

	private JBList<EntryDescription> _jbList;
	private Project _project;

	@SuppressWarnings("unused")
	private JPanel _rootPanel;

	private String _sourceJar;
	private JPanel _sourcePanel;

}