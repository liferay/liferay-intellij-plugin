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

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * @author Terry Jia
 */
public class LiferayModuleSpringMvcWizardStep extends ModuleWizardStep implements LiferayWorkspaceSupport {

	public LiferayModuleSpringMvcWizardStep(WizardContext wizardContext, LiferayModuleSpringMvcBuilder builder) {
		_builder = builder;

		String liferayVersion = getLiferayVersion(wizardContext.getProject());

		Set<String> supportedFrameworks = _getSupportedFrameworks(liferayVersion);

		for (String supportedFramework : supportedFrameworks) {
			_framework.addItem(supportedFramework);
		}

		Set<String> supportedFrameworkDependencies = _getSupportedFrameworkDependencies(
			liferayVersion, (String)_framework.getSelectedItem());

		for (String supportedFrameworkDependency : supportedFrameworkDependencies) {
			_frameworkDependencies.addItem(supportedFrameworkDependency);
		}

		Set<String> supportedViewTypes = _getSupportedViewTypes(
			liferayVersion, (String)_framework.getSelectedItem(), (String)_frameworkDependencies.getSelectedItem());

		for (String supportedViewType : supportedViewTypes) {
			_viewType.addItem(supportedViewType);
		}

		_framework.addItemListener(
			e -> {
				_frameworkDependencies.removeAllItems();

				Set<String> frameworkDependencies = _getSupportedFrameworkDependencies(liferayVersion, _getFramework());

				for (String frameworkDependency : frameworkDependencies) {
					_frameworkDependencies.addItem(frameworkDependency);
				}
			});

		_frameworkDependencies.addItemListener(
			e -> {
				_viewType.removeAllItems();

				Set<String> viewTypes = _getSupportedViewTypes(
					liferayVersion, _getFramework(), _getFrameworkDependencies());

				for (String viewType : viewTypes) {
					_viewType.addItem(viewType);
				}
			});
	}

	@Override
	public JComponent getComponent() {
		return _mainPanel;
	}

	@Override
	public void updateDataModel() {
		_builder.setFramework(_getFramework());
		_builder.setFrameworkDependencies(_getFrameworkDependencies());
		_builder.setViewType(_getViewType());
	}

	private String _getFramework() {
		Object framework = _framework.getSelectedItem();

		if (framework != null) {
			return String.valueOf(_framework.getSelectedItem());
		}

		return "";
	}

	private String _getFrameworkDependencies() {
		Object frameworkDependencies = _frameworkDependencies.getSelectedItem();

		if (frameworkDependencies != null) {
			return String.valueOf(_frameworkDependencies.getSelectedItem());
		}

		return "";
	}

	private Set<String> _getSupportedFrameworkDependencies(String liferayVersion, String framework) {
		Set<String> supportedFrameworksDependencies = new HashSet<>();

		for (String[] possibleOptions : _possibleOptionsMatrix) {
			if (liferayVersion.equals(possibleOptions[3]) && framework.equals(possibleOptions[0])) {
				supportedFrameworksDependencies.add(possibleOptions[1]);
			}
		}

		return supportedFrameworksDependencies;
	}

	private Set<String> _getSupportedFrameworks(String liferayVersion) {
		Set<String> supportedFrameworks = new HashSet<>();

		for (String[] possibleOptions : _possibleOptionsMatrix) {
			if (liferayVersion.equals(possibleOptions[3])) {
				supportedFrameworks.add(possibleOptions[0]);
			}
		}

		return supportedFrameworks;
	}

	private Set<String> _getSupportedViewTypes(String liferayVersion, String framework, String frameworkDependency) {
		Set<String> supportedViewTypes = new HashSet<>();

		for (String[] possibleOptions : _possibleOptionsMatrix) {
			if (liferayVersion.equals(possibleOptions[3]) && framework.equals(possibleOptions[0]) &&
				frameworkDependency.equals(possibleOptions[1])) {

				supportedViewTypes.add(possibleOptions[2]);
			}
		}

		return supportedViewTypes;
	}

	private String _getViewType() {
		Object viewType = _viewType.getSelectedItem();

		if (viewType != null) {
			return String.valueOf(_viewType.getSelectedItem());
		}

		return "";
	}

	private LiferayModuleSpringMvcBuilder _builder;
	private JComboBox _framework;
	private JComboBox _frameworkDependencies;
	private JPanel _mainPanel;

	/**
	 according to https://github.com/gamerson/liferay-portal/pull/279#issuecomment-500082302
	 */
	private String[][] _possibleOptionsMatrix = {
		{"springportletmvc", "embedded", "jsp", "7.0"}, {"springportletmvc", "embedded", "thymeleaf", "7.0"},
		{"springportletmvc", "embedded", "jsp", "7.1"}, {"springportletmvc", "embedded", "thymeleaf", "7.1"},
		{"springportletmvc", "provided", "jsp", "7.1"}, {"springportletmvc", "provided", "thymeleaf", "7.1"},
		{"springportletmvc", "embedded", "jsp", "7.2"}, {"springportletmvc", "embedded", "thymeleaf", "7.2"},
		{"springportletmvc", "provided", "jsp", "7.2"}, {"springportletmvc", "provided", "thymeleaf", "7.2"},
		{"springportletmvc", "embedded", "jsp", "7.3"}, {"springportletmvc", "embedded", "thymeleaf", "7.3"},
		{"portletmvc4spring", "embedded", "jsp", "7.1"}, {"portletmvc4spring", "embedded", "thymeleaf", "7.1"},
		{"portletmvc4spring", "embedded", "jsp", "7.2"}, {"portletmvc4spring", "embedded", "thymeleaf", "7.2"},
		{"portletmvc4spring", "embedded", "jsp", "7.3"}, {"portletmvc4spring", "embedded", "thymeleaf", "7.3"}
	};

	private JComboBox _viewType;

}