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

package com.liferay.ide.idea.server;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.util.Computable;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;

import com.liferay.blade.gradle.tooling.ProjectInfo;
import com.liferay.ide.idea.util.GradleUtil;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Simon Jiang
 */
public class LiferayDockerServerConfigurationEditor
	extends SettingsEditor<LiferayDockerServerConfiguration> implements PanelWithAnchor {

	public LiferayDockerServerConfigurationEditor(Project project) {
		_project = project;

		ModulesComboBox modulesComboBox = _modules.getComponent();

		modulesComboBox.allowEmptySelection("<whole project>");
		modulesComboBox.fillModules(project);

		_liferayWorkspaceProject.setEditable(false);
		_dockerImageId.setEditable(false);
		_dockerContainerId.setEditable(false);

		_dockerImageId.setText("loading...");
		_dockerContainerId.setText("loading...");

		_userActivityListener = () -> {
			Application application = ApplicationManager.getApplication();

			application.invokeLater(
				new Runnable() {

					@Override
					public void run() {
						Computable<ProjectInfo> computable = new Computable<>() {

							@Override
							public ProjectInfo compute() {
								try {
									return GradleUtil.getModel(
										ProjectInfo.class, ProjectUtil.guessProjectDir(_project));
								}
								catch (Exception exception) {
								}

								return null;
							}

						};

						SwingUtilities.invokeLater(
							() -> {
								try {
									ProjectInfo projectInfo = computable.get();

									if (projectInfo != null) {
										_dockerImageId.setText(projectInfo.getDockerImageId());
										_dockerContainerId.setText(projectInfo.getDockerContainerId());
									}
								}
								catch (Exception exception) {
								}
							});
					}

				});
		};

		_userActivityWatcher = new UserActivityWatcher();

		_userActivityWatcher.addUserActivityListener(_userActivityListener);
		_userActivityWatcher.register(_liferayWorkspaceProject);

		_liferayWorkspaceProject.setText(project.getName());
	}

	public void applyEditorTo(@NotNull LiferayDockerServerConfiguration configuration) throws ConfigurationException {
		ModulesComboBox modulesComboBox = _modules.getComponent();

		configuration.setDockerImageId(_dockerImageId.getText());
		configuration.setDockerContainerId(_dockerContainerId.getText());
		configuration.setModule(modulesComboBox.getSelectedModule());

		configuration.checkConfiguration();
	}

	@NotNull
	@Override
	public JComponent createEditor() {
		return _mainPanel;
	}

	public JComponent getAnchor() {
		return _anchor;
	}

	@Override
	public void setAnchor(@Nullable JComponent anchor) {
		_anchor = anchor;
	}

	@Override
	protected void disposeEditor() {
		_userActivityWatcher.removeUserActivityListener(_userActivityListener);

		_userActivityWatcher = null;
	}

	@Override
	protected void resetEditorFrom(@NotNull LiferayDockerServerConfiguration configuration) {
	}

	private JComponent _anchor;
	private JTextField _dockerContainerId;
	private JTextField _dockerImageId;
	private JTextField _liferayWorkspaceProject;
	private JPanel _mainPanel;
	private LabeledComponent<ModulesComboBox> _modules;
	private Project _project;
	private UserActivityListener _userActivityListener;
	private UserActivityWatcher _userActivityWatcher;

}