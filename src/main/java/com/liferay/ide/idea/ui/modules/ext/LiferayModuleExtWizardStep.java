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

import com.google.common.collect.Lists;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.util.ui.UIUtil;

import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.awt.event.ItemEvent;

import java.util.Objects;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.service.task.GradleTaskManager;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import org.osgi.framework.Version;

/**
 * @author Charles Wu
 * @author Terry Jia
 * @author Simon Jiang
 * @author Seiphon Wang
 */
public class LiferayModuleExtWizardStep extends ModuleWizardStep implements LiferayWorkspaceSupport {

	public LiferayModuleExtWizardStep(WizardContext wizardContext, LiferayModuleExtBuilder liferayModuleExtBuilder) {
		_project = wizardContext.getProject();

		_workspacePluginVersion = GradleUtil.getWorkspacePluginVersion(_project);

		_liferayModuleExtBuilder = liferayModuleExtBuilder;

		_moduleNameHintLabel.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));

		// customize the presentation of a artifact

		_originalModuleNameComboBox.setRenderer(
			new ColoredListCellRenderer<String>() {

				@Override
				protected void customizeCellRenderer(
					@NotNull JList<? extends String> list, String value, int index, boolean selected,
					boolean hasFocus) {

					append(value);
				}

			});

		// only set the artifact name when select the value from list.

		_originalModuleNameComboBox.setEditor(
			new BasicComboBoxEditor() {

				@Override
				public void setItem(Object item) {
					String text = (String)item;

					editor.setText(text);
				}

			});

		// fill out the module version field automatic

		_originalModuleNameComboBox.addItemListener(
			event -> {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					Object item = event.getItem();

					String dependency = (String)item;

					if (CoreUtil.compareVersions(new Version(_workspacePluginVersion), new Version("2.2.4")) < 0) {
						String[] s = dependency.split(" ");

						if (s.length == 2) {
							_originalModuleVersionField.setText(s[1]);
						}
					}
					else {
						String[] s = dependency.split(":");

						if (s.length == 3) {
							_originalModuleVersionField.setText(s[2]);
						}
					}
				}
			});

		Application application = ApplicationManager.getApplication();

		application.executeOnPooledThread(this::_insertOriginalModuleNames);

		_originalModuleNameComboBox.setMaximumRowCount(12);
		_originalModuleVersionField.setEnabled(false);
	}

	@Override
	public JComponent getComponent() {
		return _mainPanel;
	}

	@Override
	public void updateDataModel() {
		_liferayModuleExtBuilder.setOriginalModuleName(_getOriginalModuleName());
		_liferayModuleExtBuilder.setOriginalModuleVersion(_originalModuleVersionField.getText());
	}

	@Override
	public boolean validate() throws ConfigurationException {
		String validationTitle = "Validation Error";

		if (getTargetPlatformVersion(_project) == null) {
			throw new ConfigurationException(
				"Please set target platform version for liferay workspace project", validationTitle);
		}

		if (_originalModuleNameComboBox.getItemCount() == 0) {
			throw new ConfigurationException("No valid original modules can be selected", validationTitle);
		}

		if (CoreUtil.isNullOrEmpty(_getOriginalModuleName())) {
			throw new ConfigurationException("Please input original module name", validationTitle);
		}
		else if ((getTargetPlatformVersion(_project) == null) &&
				 CoreUtil.isNullOrEmpty(_originalModuleVersionField.getText())) {

			throw new ConfigurationException("Please input original module version", validationTitle);
		}

		if (Objects.isNull(getLiferayVersion(_project))) {
			throw new ConfigurationException(
				"The property `liferay.workspace.product` or `liferay.workspace.target.platform.version` has not " +
					"been set. One of these properties must be set in order to continue.",
				validationTitle);
		}

		if (CoreUtil.compareVersions(Version.parseVersion(getLiferayVersion(_project)), Version.parseVersion("7.0")) <=
				0) {

			throw new ConfigurationException(
				"Module Ext Projects only work on Liferay Workspace which version is greater than 7.0",
				validationTitle);
		}

		return true;
	}

	private ComboBoxEditor _getEditor() {
		return _originalModuleNameComboBox.getEditor();
	}

	private String _getOriginalModuleName() {
		Object item = _getEditor().getItem();

		String s = (String)item;

		if (CoreUtil.compareVersions(new Version(_workspacePluginVersion), new Version("2.2.4")) < 0) {
			int i1 = s.indexOf(":");
			int i2 = s.indexOf(" ");

			if ((i1 > -1) && (i2 > -1)) {
				return s.substring(i1 + 1, i2);
			}
		}
		else {
			return s.split(":")[1];
		}

		return s;
	}

	private void _insertOriginalModuleNames() {
		if (Objects.isNull(getTargetPlatformVersion(_project))) {
			return;
		}

		GradleTaskManager gradleTaskManager = new GradleTaskManager();

		final boolean[] dependencyStringStart = {false};

		gradleTaskManager.executeTasks(
			ExternalSystemTaskId.create(GradleConstants.SYSTEM_ID, ExternalSystemTaskType.EXECUTE_TASK, _project),
			Lists.newArrayList("dependencyManagement"), _project.getBasePath(), null, null,
			new ExternalSystemTaskNotificationListenerAdapter() {

				@Override
				public void onTaskOutput(@NotNull ExternalSystemTaskId id, @NotNull String text, boolean stdOut) {
					String projectId = ExternalSystemTaskId.getProjectId(_project);

					if (!projectId.equals(id.getIdeProjectId())) {
						return;
					}

					if (text.contains("Task :dependencyManagement")) {
						dependencyStringStart[0] = true;

						return;
					}

					if (dependencyStringStart[0] == true) {
						String dependencyStringRemoveT = text.replace("\t", "");

						String dependencyString = dependencyStringRemoveT.replace("\n", "");

						String[] dependencyValues = dependencyString.split(":");

						if (dependencyValues[0].equals("com.liferay")) {
							SwingUtilities.invokeLater(
								new Runnable() {

									@Override
									public void run() {
										_originalModuleNameComboBox.addItem(dependencyString);
									}

								});
						}
					}
				}

			});
	}

	private LiferayModuleExtBuilder _liferayModuleExtBuilder;
	private JPanel _mainPanel;
	private JLabel _moduleNameHintLabel;
	private JComboBox<String> _originalModuleNameComboBox;
	private JTextField _originalModuleVersionField;
	private final Project _project;
	private String _workspacePluginVersion;

}