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

package com.liferay.ide.idea.ui.modules.springmvcportlet;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.file.PsiDirectoryFactory;

import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Terry Jia
 * @author Simon Jiang
 * @author Ethan Sun
 */
public class SpringMvcPortletModuleWizardStep extends ModuleWizardStep implements LiferayWorkspaceSupport {

	public SpringMvcPortletModuleWizardStep(WizardContext wizardContext, SpringMVCPortletModuleBuilder builder) {
		_builder = builder;

		_project = wizardContext.getProject();

		_liferayVersion = getLiferayVersion(_project);

		if (Objects.isNull(_liferayVersion)) {
			_liferayVersion = WorkspaceConstants.DEFAULT_LIFERAY_VERSION;

			_liferayVersionCombo.removeAllItems();

			for (String liferayVersionItem : WorkspaceConstants.LIFERAY_VERSIONS) {
				_liferayVersionCombo.addItem(liferayVersionItem);
			}

			_liferayVersionCombo.setSelectedItem(_liferayVersion);

			_initializeSpringConfigurationData();

			_frameworkCombo.addActionListener(
				e -> {
					if (_frameworkCombo.getSelectedItem() != null) {
						_rendererFrameworkDependenciesComboItems();
					}
				});

			_liferayVersionCombo.addActionListener(
				e -> {
					_liferayVersion = (String)_liferayVersionCombo.getSelectedItem();

					_rendererFrameworkComboItems();
				});
		}
		else {
			_mainPanel.remove(_liferayVersionLabel);

			_mainPanel.remove(_liferayVersionCombo);

			_mainPanel.repaint();

			_initializeSpringConfigurationData();

			_frameworkCombo.addActionListener(
				e -> {
					if (_frameworkCombo.getSelectedItem() != null) {
						_rendererFrameworkDependenciesComboItems();
					}
				});
		}
	}

	@Override
	public JComponent getComponent() {
		return _mainPanel;
	}

	public String getPackageName() {
		String packageName = _packageName.getText();

		if (!CoreUtil.isNullOrEmpty(packageName)) {
			packageName = packageName.replace('-', '.');

			packageName = packageName.replace(' ', '.');

			return packageName.toLowerCase();
		}

		return null;
	}

	@Override
	public void updateDataModel() {
		Map<String, String> frameworkDependeices = SpringMVCPortletProjectConstants.springFrameworkDependeices;
		Map<String, String> frameworks = SpringMVCPortletProjectConstants.springFrameworks;
		Map<String, String> viewTypes = SpringMVCPortletProjectConstants.springViewTypes;

		_builder.setFramework(frameworks.get(_frameworkCombo.getSelectedItem()));
		_builder.setFrameworkDependencies(frameworkDependeices.get(_frameworkDependenciesCombo.getSelectedItem()));
		_builder.setLiferayVersion(_liferayVersion);
		_builder.setViewType(viewTypes.get(_viewTypeCombo.getSelectedItem()));
		_builder.setPackageName(getPackageName());
	}

	@Override
	public boolean validate() throws ConfigurationException {
		String packageName = _packageName.getText();

		if (!CoreUtil.isNullOrEmpty(packageName)) {
			PsiDirectoryFactory psiDirectoryFactory = PsiDirectoryFactory.getInstance(Objects.requireNonNull(_project));

			if (!psiDirectoryFactory.isValidPackageName(packageName)) {
				throw new ConfigurationException(packageName + " is not a valid package name", "Validation Error");
			}
		}

		return true;
	}

	private void _addComboItems(String[] values, JComboBox<String> comboBox) {
		Stream.of(
			values
		).forEach(
			item -> comboBox.addItem(item)
		);
	}

	private void _clearSpringConfigurationData() {
		if (_frameworkCombo != null) {
			_frameworkCombo.removeAllItems();
		}

		if (_frameworkCombo != null) {
			_frameworkDependenciesCombo.removeAllItems();
		}

		if (_frameworkCombo != null) {
			_viewTypeCombo.removeAllItems();
		}
	}

	private void _initializeSpringConfigurationData() {
		_clearSpringConfigurationData();

		if (_liferayVersion.equals(WorkspaceConstants.LIFERAY_VERSIONS[3])) {
			_frameworkCombo.addItem(SpringMVCPortletProjectConstants.SPRING_FRAMEWORK[1]);
		}
		else {
			_addComboItems(SpringMVCPortletProjectConstants.SPRING_FRAMEWORK, _frameworkCombo);
		}

		if (_liferayVersion.equals(WorkspaceConstants.LIFERAY_VERSIONS[0])) {
			_addComboItems(SpringMVCPortletProjectConstants.SPRING_FRAMEWORK_DEPENDENCIES, _frameworkDependenciesCombo);
		}
		else {
			_frameworkDependenciesCombo.addItem(SpringMVCPortletProjectConstants.SPRING_FRAMEWORK_DEPENDENCIES[0]);
		}

		_addComboItems(SpringMVCPortletProjectConstants.SPRING_VIEW_TYPE, _viewTypeCombo);
		_frameworkCombo.setSelectedIndex(0);
		_frameworkDependenciesCombo.setSelectedIndex(0);
		_viewTypeCombo.setSelectedIndex(0);
	}

	private void _rendererFrameworkComboItems() {
		_clearSpringConfigurationData();

		if (_liferayVersion.equals(WorkspaceConstants.LIFERAY_VERSIONS[3])) {
			_frameworkCombo.addItem(SpringMVCPortletProjectConstants.SPRING_FRAMEWORK[1]);
		}
		else {
			_addComboItems(SpringMVCPortletProjectConstants.SPRING_FRAMEWORK, _frameworkCombo);
		}

		_addComboItems(SpringMVCPortletProjectConstants.SPRING_VIEW_TYPE, _viewTypeCombo);
	}

	private void _rendererFrameworkDependenciesComboItems() {
		_frameworkDependenciesCombo.removeAllItems();

		String frameworkSelectedItem = (String)_frameworkCombo.getSelectedItem();

		if (frameworkSelectedItem.equals(SpringMVCPortletProjectConstants.SPRING_FRAMEWORK[0])) {
			_frameworkDependenciesCombo.removeAllItems();

			if (_liferayVersion.equals(WorkspaceConstants.LIFERAY_VERSIONS[0])) {
				_addComboItems(
					SpringMVCPortletProjectConstants.SPRING_FRAMEWORK_DEPENDENCIES, _frameworkDependenciesCombo);
			}
			else {
				_frameworkDependenciesCombo.addItem(SpringMVCPortletProjectConstants.SPRING_FRAMEWORK_DEPENDENCIES[0]);
			}
		}
		else if (frameworkSelectedItem.equals(SpringMVCPortletProjectConstants.SPRING_FRAMEWORK[1])) {
			_frameworkDependenciesCombo.removeAllItems();

			if (_liferayVersion.equals(WorkspaceConstants.LIFERAY_VERSIONS[1]) ||
				_liferayVersion.equals(WorkspaceConstants.LIFERAY_VERSIONS[2])) {

				_addComboItems(
					SpringMVCPortletProjectConstants.SPRING_FRAMEWORK_DEPENDENCIES, _frameworkDependenciesCombo);
			}
			else {
				_frameworkDependenciesCombo.addItem(SpringMVCPortletProjectConstants.SPRING_FRAMEWORK_DEPENDENCIES[0]);
			}
		}

		_frameworkDependenciesCombo.setSelectedIndex(0);
	}

	private SpringMVCPortletModuleBuilder _builder;
	private JComboBox<String> _frameworkCombo;
	private JComboBox<String> _frameworkDependenciesCombo;
	private String _liferayVersion;
	private JComboBox<String> _liferayVersionCombo;
	private JLabel _liferayVersionLabel;
	private JPanel _mainPanel;
	private JTextField _packageName;
	private final Project _project;
	private JComboBox<String> _viewTypeCombo;

}