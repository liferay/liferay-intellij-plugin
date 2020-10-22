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
import com.intellij.execution.ui.DefaultJreSelector;
import com.intellij.execution.ui.JrePathEditor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;

import com.liferay.ide.idea.server.portal.PortalBundle;
import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ServerUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class LiferayServerConfigurable
	extends SettingsEditor<LiferayServerConfiguration> implements LiferayWorkspaceSupport, PanelWithAnchor {

	public LiferayServerConfigurable(Project project) {
		ModulesComboBox modulesComboBox = _modules.getComponent();

		modulesComboBox.allowEmptySelection("<whole project>");
		modulesComboBox.fillModules(project);

		_bundleType.setEnabled(false);
		_liferayServer.setEnabled(true);

		_liferayServer.addBrowseFolderListener(
			"Liferay installation folder", "Choose the folder where Liferay is installed (e.g. bundles)", project,
			FileChooserDescriptorFactory.createSingleFolderDescriptor());

		_jrePath.setDefaultJreSelector(DefaultJreSelector.fromModuleDependencies(modulesComboBox, true));

		_userActivityListener = () -> {
			Application application = ApplicationManager.getApplication();

			application.runWriteAction(
				() -> {
					PortalBundle portalBundle = ServerUtil.getPortalBundle(FileUtil.getPath(_liferayServer.getText()));

					if (portalBundle != null) {
						_bundleType.setText(portalBundle.getType());
					}
					else {
						_bundleType.setText("");
					}
				});
		};

		_userActivityWatcher = new UserActivityWatcher();

		_userActivityWatcher.addUserActivityListener(_userActivityListener);
		_userActivityWatcher.register(_liferayServer);
	}

	@Override
	public void applyEditorTo(@NotNull LiferayServerConfiguration configuration) throws ConfigurationException {
		ModulesComboBox modulesComboBox = _modules.getComponent();

		configuration.setAlternativeJrePath(_jrePath.getJrePathOrName());
		configuration.setAlternativeJrePathEnabled(_jrePath.isAlternativeJreSelected());
		configuration.setBundleLocation(_liferayServer.getText());
		configuration.setBundleType(_bundleType.getText());
		configuration.setDeveloperMode(_developerMode.isSelected());
		configuration.setModule(modulesComboBox.getSelectedModule());
		configuration.setVMParameters(_vmParams.getText());

		configuration.checkConfiguration();
	}

	@NotNull
	@Override
	public JComponent createEditor() {
		return _mainPanel;
	}

	@Override
	public JComponent getAnchor() {
		return _anchor;
	}

	@Override
	public void resetEditorFrom(@NotNull LiferayServerConfiguration configuration) {
		_bundleType.setEnabled(false);
		_vmParams.setText(configuration.getVMParameters());
		PortalBundle portalBundle = ServerUtil.getPortalBundle(FileUtil.getPath(configuration.getBundleLocation()));

		if (portalBundle != null) {
			Path appServerDir = portalBundle.getAppServerDir();

			_liferayServer.setText(appServerDir.toString());

			_bundleType.setText(portalBundle.getType());
		}
		else {
			Project project = configuration.getProject();

			String basePath = project.getBasePath();

			Path path = Paths.get(getHomeDir(project));

			if (!path.isAbsolute()) {
				path = Paths.get(basePath, getHomeDir(project));
			}

			_liferayServer.setText(path.toString());
		}

		_jrePath.setPathOrName(configuration.getAlternativeJrePath(), configuration.isAlternativeJrePathEnabled());
		_developerMode.setSelected(configuration.getDeveloperMode());

		ModulesComboBox modulesComboBox = _modules.getComponent();

		modulesComboBox.setSelectedModule(configuration.getModule());
	}

	@Override
	public void setAnchor(@Nullable JComponent anchor) {
		_anchor = anchor;
		_jrePath.setAnchor(anchor);
	}

	@Override
	protected void disposeEditor() {
		_userActivityWatcher.removeUserActivityListener(_userActivityListener);

		_userActivityWatcher = null;
	}

	private JComponent _anchor;
	private JTextField _bundleType;
	private JCheckBox _developerMode;
	private JrePathEditor _jrePath;
	private TextFieldWithBrowseButton _liferayServer;
	private JPanel _mainPanel;
	private LabeledComponent<ModulesComboBox> _modules;
	private UserActivityListener _userActivityListener;
	private UserActivityWatcher _userActivityWatcher;
	private JTextField _vmParams;

}