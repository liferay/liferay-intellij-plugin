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

package com.liferay.ide.idea.ui.modules.spring;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;

import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.WorkspaceConstants;

import java.util.Map;
import java.util.stream.Stream;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class LiferayModuleSpringMvcWizardStep extends ModuleWizardStep implements LiferayWorkspaceSupport {

	public LiferayModuleSpringMvcWizardStep(WizardContext wizardContext, LiferayModuleSpringMvcBuilder builder) {
		_builder = builder;

		String liferayVersion = getLiferayVersion(wizardContext.getProject());

		_intializeSpringConfigurationData(liferayVersion);

		_frameworkCombo.addItemListener(
			e -> {
				_frameworkDependenciesCombo.removeAllItems();

				String value = (String)_frameworkCombo.getSelectedItem();

				if (value.equals(WorkspaceConstants.SPRING_FRAMEWORK[0])) {
					_frameworkDependenciesCombo.removeAllItems();
					_frameworkDependenciesCombo.addItem(WorkspaceConstants.SPRING_FRAMEWORK_DEPENDENCIES[0]);
				}
				else if (value.equals(WorkspaceConstants.SPRING_FRAMEWORK[1])) {
					_frameworkDependenciesCombo.removeAllItems();

					if (liferayVersion.equals(WorkspaceConstants.LIFERAY_VERSIONS[1]) ||
						liferayVersion.equals(WorkspaceConstants.LIFERAY_VERSIONS[2])) {

						_addComboItems(WorkspaceConstants.SPRING_FRAMEWORK_DEPENDENCIES, _frameworkDependenciesCombo);
					}
					else {
						_frameworkDependenciesCombo.addItem(WorkspaceConstants.SPRING_FRAMEWORK_DEPENDENCIES[0]);
					}
				}

				_frameworkDependenciesCombo.setSelectedIndex(0);
			});
	}

	@Override
	public JComponent getComponent() {
		return _mainPanel;
	}

	@Override
	public void updateDataModel() {
		Map<String, String> frameworkDependeices = WorkspaceConstants.springFrameworkDependeices;
		Map<String, String> frameworks = WorkspaceConstants.springFrameworks;
		Map<String, String> viewTypes = WorkspaceConstants.springViewTypes;

		_builder.setFramework(frameworks.get(_frameworkCombo.getSelectedItem()));
		_builder.setFrameworkDependencies(frameworkDependeices.get(_frameworkDependenciesCombo.getSelectedItem()));
		_builder.setViewType(viewTypes.get(_viewTypeCombo.getSelectedItem()));
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
			_frameworkCombo.removeAll();
		}

		if (_frameworkCombo != null) {
			_frameworkDependenciesCombo.removeAll();
		}

		if (_frameworkCombo != null) {
			_viewTypeCombo.removeAll();
		}
	}

	private void _intializeSpringConfigurationData(String liferayVersion) {
		_clearSpringConfigurationData();

		if (liferayVersion.equals(WorkspaceConstants.LIFERAY_VERSIONS[0])) {
			_frameworkCombo.addItem(WorkspaceConstants.SPRING_FRAMEWORK[1]);
		}
		else {
			_addComboItems(WorkspaceConstants.SPRING_FRAMEWORK, _frameworkCombo);
		}

		_frameworkDependenciesCombo.addItem(WorkspaceConstants.SPRING_FRAMEWORK_DEPENDENCIES[0]);
		_addComboItems(WorkspaceConstants.SPRING_VIEW_TYPE, _viewTypeCombo);

		_frameworkCombo.setSelectedIndex(0);
		_frameworkDependenciesCombo.setSelectedIndex(0);
		_viewTypeCombo.setSelectedIndex(0);
	}

	private LiferayModuleSpringMvcBuilder _builder;
	private JComboBox<String> _frameworkCombo;
	private JComboBox<String> _frameworkDependenciesCombo;
	private JPanel _mainPanel;
	private JComboBox<String> _viewTypeCombo;

	/**
	 according to https://github.com/gamerson/liferay-portal/pull/279#issuecomment-500082302
	 */
}