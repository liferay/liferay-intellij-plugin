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

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;

import com.liferay.ide.idea.ui.compoments.FixedSizeRefreshButton;
import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Charle Wu
 */
public class LiferayModuleExtWizardStep extends ModuleWizardStep {

	public LiferayModuleExtWizardStep(WizardContext wizardContext, LiferayModuleExtBuilder builder) {
		_project = wizardContext.getProject();
		_builder = builder;

		_moduleNameHint.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));

		// customize the presentation of a artifact

		_originalModuleName.setRenderer(
			new ColoredListCellRenderer<LibraryData>() {

				@Override
				protected void customizeCellRenderer(
					@NotNull JList<? extends LibraryData> list, LibraryData value, int index, boolean selected,
					boolean hasFocus) {

					append(value.getArtifactId());
					append("  " + value.getVersion(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
				}

			});

		// only set the artifact name when select the value from list.

		_originalModuleName.setEditor(
			new BasicComboBoxEditor() {

				@Override
				public void setItem(Object item) {
					if (item instanceof LibraryData) {
						String text = ((LibraryData)item).getArtifactId();

						if (!text.equals(editor.getText())) {
							editor.setText(text);
						}
					}
				}

			});

		// fill out the module version field automatic

		_originalModuleName.addItemListener(
			event -> {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					Object item = event.getItem();

					if (item instanceof LibraryData) {
						_originalModuleVersion.setText(((LibraryData)item).getVersion());
					}
				}
			});

		if (LiferayWorkspaceUtil.getTargetPlatformVersion(_project) != null) {
			_insertOriginalModuleNames(false);

			_originalModuleName.setMaximumRowCount(12);
			_originalModuleVersion.setEnabled(false);
		}

		_refreshButton.addActionListener(
			new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					_refreshButton.setEnabled(false);

					if (LiferayWorkspaceUtil.getTargetPlatformVersion(_project) == null) {
						Messages.showMessageDialog(
							_project, "No Target Platform configuration claimed in gradle.properties.", "Warning",
							Messages.getWarningIcon());
						_refreshButton.setEnabled(true);

						return;
					}

					ImportSpecBuilder builder = new ImportSpecBuilder(_project, GradleConstants.SYSTEM_ID);

					builder.use(ProgressExecutionMode.START_IN_FOREGROUND_ASYNC);
					builder.callback(
						new ExternalProjectRefreshCallback() {

							@Override
							public void onFailure(@NotNull String errorMessage, @Nullable String errorDetails) {
								_refreshButton.setEnabled(true);
							}

							@Override
							public void onSuccess(@Nullable DataNode<ProjectData> externalProject) {
								Application application = ApplicationManager.getApplication();

								application.invokeLater(() -> _insertOriginalModuleNames(true));

								_refreshButton.setEnabled(true);
							}

						});

					ExternalSystemUtil.refreshProjects(builder);
				}

			});
	}

	@Override
	public JComponent getComponent() {
		return _mainPanel;
	}

	@Override
	public void updateDataModel() {
		_builder.setOriginalModuleName(_getOriginalModuleName());

		_builder.setOriginalModuleVersion(_originalModuleVersion.getText());
	}

	@Override
	public boolean validate() throws ConfigurationException {
		String validationTitle = "Validation Error";

		if (CoreUtil.isNullOrEmpty(_getOriginalModuleName())) {
			throw new ConfigurationException("Please input original module name", validationTitle);
		}
		else if ((LiferayWorkspaceUtil.getTargetPlatformVersion(_project) == null) &&
				 CoreUtil.isNullOrEmpty(_originalModuleVersion.getText())) {

			throw new ConfigurationException("Please input original module version", validationTitle);
		}

		return true;
	}

	private String _getOriginalModuleName() {
		ComboBoxEditor editor = _originalModuleName.getEditor();

		Object item = editor.getItem();

		return item.toString();
	}

	private void _insertOriginalModuleNames(boolean clear) {
		List<LibraryData> targetPlatformArtifacts = GradleUtil.getTargetPlatformArtifacts(_project);

		if (clear) {
			try {
				_originalModuleName.removeAllItems();
			}
			catch (NullPointerException npe) {
			}
		}

		targetPlatformArtifacts.forEach(
			artifact -> {
				if ("com.liferay".equals(artifact.getGroupId())) {
					_originalModuleName.addItem(artifact);
				}
			});
	}

	private LiferayModuleExtBuilder _builder;
	private JPanel _mainPanel;
	private JLabel _moduleNameHint;
	private JComboBox<LibraryData> _originalModuleName;
	private JTextField _originalModuleVersion;
	private final Project _project;
	private FixedSizeRefreshButton _refreshButton;

}