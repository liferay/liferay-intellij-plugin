/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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

import java.util.Map;
import java.util.Objects;

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
public class LiferayServerConfigurable extends SettingsEditor<LiferayServerConfiguration> implements PanelWithAnchor {

	public LiferayServerConfigurable(Project project) {
		ModulesComboBox modulesComboBox = _modules.getComponent();

		modulesComboBox.allowEmptySelection("<whole project>");
		modulesComboBox.fillModules(project);

		_bundleType.setEnabled(false);
		_liferayServer.setEnabled(true);
		_gogoShellPort.setEnabled(true);

		_liferayServer.addBrowseFolderListener(
			project,
			FileChooserDescriptorFactory.createSingleFolderDescriptor(
			).withDescription(
				"Choose the folder where Liferay is installed (e.g. bundles)"
			).withTitle(
				"Liferay Installation Folder"
			));

		_jrePath.setDefaultJreSelector(DefaultJreSelector.fromModuleDependencies(modulesComboBox, true));

		_userEnvironment.setEnabled(true);
		_userEnvironment.setPassParentEnvs(false);

		_userActivityListener = () -> {
			Application application = ApplicationManager.getApplication();

			application.runWriteAction(
				() -> {
					PortalBundle portalBundle = ServerUtil.getPortalBundle(FileUtil.getPath(_liferayServer.getText()));

					if (portalBundle != null) {
						_bundleType.setText(portalBundle.getDisplayName());
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
		configuration.setAlternativeJrePath(_jrePath.getJrePathOrName());
		configuration.setAlternativeJrePathEnabled(_jrePath.isAlternativeJreSelected());

		configuration.setBundleLocation(_liferayServer.getText());

		PortalBundle portalBundle = ServerUtil.getPortalBundle(Paths.get(_liferayServer.getText()));

		if (portalBundle != null) {
			configuration.setBundleType(portalBundle.getType());
		}
		else {
			throw new ConfigurationException("Portal bundle type is invalid");
		}

		ModulesComboBox modulesComboBox = _modules.getComponent();

		configuration.setDeveloperMode(_developerMode.isSelected());
		configuration.setModule(modulesComboBox.getSelectedModule());
		configuration.setVMParameters(_vmParams.getText());
		configuration.setGogoShellPort(_gogoShellPort.getText());

		configuration.setEnvs(_userEnvironment.getEnvs());

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

	public Map<String, String> getEnv() {
		return _userEnvironment.getEnvs();
	}

	@Override
	public void resetEditorFrom(@NotNull LiferayServerConfiguration configuration) {
		_bundleType.setEnabled(false);

		_vmParams.setText(configuration.getVMParameters());

		PortalBundle portalBundle = ServerUtil.getPortalBundle(FileUtil.getPath(configuration.getBundleLocation()));

		if (portalBundle != null) {
			_liferayServer.setText(String.valueOf(portalBundle.getAppServerDir()));

			_bundleType.setText(portalBundle.getDisplayName());
		}
		else {
			Project project = configuration.getProject();

			String basePath = project.getBasePath();

			String bundleDir = LiferayWorkspaceSupport.getHomeDir(project);

			if (Objects.nonNull(bundleDir)) {
				Path path = Paths.get(bundleDir);

				if (!path.isAbsolute()) {
					path = Paths.get(basePath, LiferayWorkspaceSupport.getHomeDir(project));
				}

				_liferayServer.setText(path.toString());
			}
		}

		_gogoShellPort.setText(configuration.getGogoShellPort());

		_jrePath.setPathOrName(configuration.getAlternativeJrePath(), configuration.isAlternativeJrePathEnabled());
		_developerMode.setSelected(configuration.getDeveloperMode());

		_userEnvironment.setEnvs(configuration.getEnvs());

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
	private JTextField _gogoShellPort;
	private JrePathEditor _jrePath;
	private TextFieldWithBrowseButton _liferayServer;
	private JPanel _mainPanel;
	private LabeledComponent<ModulesComboBox> _modules;
	private UserActivityListener _userActivityListener;
	private UserActivityWatcher _userActivityWatcher;
	private LiferayEnvironmentVariablesTextFieldWithBrowseButton _userEnvironment;
	private JTextField _vmParams;

}