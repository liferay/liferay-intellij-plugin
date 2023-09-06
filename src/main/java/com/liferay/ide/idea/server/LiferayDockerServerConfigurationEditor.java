/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;

import com.liferay.blade.gradle.tooling.ProjectInfo;
import com.liferay.ide.idea.util.GradleUtil;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
						CompletableFuture<ProjectInfo> future = CompletableFuture.supplyAsync(
							() -> {
								try {
									return GradleUtil.getModel(
										ProjectInfo.class, ProjectUtil.guessProjectDir(_project));
								}
								catch (Exception exception) {
									return null;
								}
							});

						future.thenAccept(
							new Consumer<ProjectInfo>() {

								@Override
								public void accept(ProjectInfo projectInfo) {
									SwingUtilities.invokeLater(
										() -> {
											try {
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