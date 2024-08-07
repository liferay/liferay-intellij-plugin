/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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

import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Terry Jia
 * @author Simon Jiang
 * @author Ethan Sun
 * @author Seiphon Wang
 */
public class SpringMvcPortletModuleWizardStep extends ModuleWizardStep {

	public SpringMvcPortletModuleWizardStep(WizardContext wizardContext, SpringMVCPortletModuleBuilder builder) {
		_builder = builder;

		_project = wizardContext.getProject();

		_liferayProductGroupVersion = LiferayWorkspaceSupport.getLiferayProductGroupVersion(_project);

		if (Objects.isNull(_liferayProductGroupVersion)) {
			_liferayProductGroupVersion = WorkspaceConstants.DEFAULT_LIFERAY_VERSION;

			_liferayProductGroupVersionCombo.removeAllItems();

			for (String liferayVersionItem : LiferayWorkspaceSupport.getProductGroupVersions()) {
				_liferayProductGroupVersionCombo.addItem(liferayVersionItem);
			}

			_liferayProductGroupVersionCombo.setSelectedItem(_liferayProductGroupVersion);

			_initializeSpringConfigurationData();

			_frameworkCombo.addActionListener(
				e -> {
					if (_frameworkCombo.getSelectedItem() != null) {
						_rendererFrameworkDependenciesComboItems();
					}
				});

			_liferayProductGroupVersionCombo.addActionListener(
				e -> {
					_liferayProductGroupVersion = (String)_liferayProductGroupVersionCombo.getSelectedItem();

					_rendererFrameworkComboItems();
				});
		}
		else {
			_mainPanel.remove(_liferayVersionLabel);

			_mainPanel.remove(_liferayProductGroupVersionCombo);

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

		_builder.setFramework(frameworks.get((String)_frameworkCombo.getSelectedItem()));
		_builder.setFrameworkDependencies(
			frameworkDependeices.get((String)_frameworkDependenciesCombo.getSelectedItem()));
		_builder.setLiferayProductGroupVersion(_liferayProductGroupVersion);
		_builder.setViewType(viewTypes.get((String)_viewTypeCombo.getSelectedItem()));
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
			comboBox::addItem
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

	private String[] _getLables(String[] values) {
		List<String> labelList = new ArrayList<>();

		Map<String, String> frameworkDependeices = SpringMVCPortletProjectConstants.springFrameworkDependeices;
		Map<String, String> frameworks = SpringMVCPortletProjectConstants.springFrameworks;
		Map<String, String> viewTypes = SpringMVCPortletProjectConstants.springViewTypes;

		Set<Map.Entry<String, String>> lableMapSet = new HashSet<>();

		lableMapSet.addAll(frameworkDependeices.entrySet());
		lableMapSet.addAll(frameworks.entrySet());
		lableMapSet.addAll(viewTypes.entrySet());

		for (String value : values) {
			boolean found = false;

			for (Map.Entry<String, String> entry : lableMapSet) {
				if (value.equals(entry.getValue())) {
					labelList.add(entry.getKey());

					found = true;

					break;
				}
			}

			if (!found) {
				labelList.add(value);
			}
		}

		return labelList.toArray(new String[0]);
	}

	private String[] _getSpringFrameworks() {
		JSONObject frameworksObject = _getSpringFrameworksJsonObject();

		if (frameworksObject != null) {
			Set<?> frameworkskeySet = frameworksObject.keySet();

			return _getLables(frameworkskeySet.toArray(new String[0]));
		}

		return new String[0];
	}

	private JSONObject _getSpringFrameworksJsonObject() {
		try (InputStream inputStream = SpringMvcPortletModuleWizardStep.class.getResourceAsStream(
				"/configurations/springmvc.json")) {

			String jsonContent = CoreUtil.readStreamToString(inputStream);

			JSONObject contentObject = new JSONObject(jsonContent);

			if (_liferayProductGroupVersion.contains("q")) {
				return (JSONObject)contentObject.get("quarterly");
			}

			return (JSONObject)contentObject.get(_liferayProductGroupVersion);
		}
		catch (Exception exception) {
		}

		return null;
	}

	private void _initializeSpringConfigurationData() {
		_clearSpringConfigurationData();

		_addComboItems(_getSpringFrameworks(), _frameworkCombo);

		_frameworkCombo.addActionListener(
			e -> {
				if (_frameworkCombo.getSelectedItem() != null) {
					_rendererFrameworkDependenciesComboItems();
				}
			});

		_frameworkCombo.setSelectedIndex(0);
	}

	private void _rendererFrameworkComboItems() {
		_clearSpringConfigurationData();

		_addComboItems(_getSpringFrameworks(), _frameworkCombo);
	}

	private void _rendererFrameworkDependenciesComboItems() {
		_frameworkDependenciesCombo.removeAllItems();

		JSONObject frameworksObject = _getSpringFrameworksJsonObject();

		if (frameworksObject != null) {
			Map<String, String> frameworks = SpringMVCPortletProjectConstants.springFrameworks;

			JSONObject dependencyObject = (JSONObject)frameworksObject.get(
				frameworks.get((String)_frameworkCombo.getSelectedItem()));

			Set<?> dependencyKeySet = dependencyObject.keySet();

			_addComboItems(_getLables(dependencyKeySet.toArray(new String[0])), _frameworkDependenciesCombo);

			_frameworkDependenciesCombo.setSelectedIndex(0);

			_viewTypeCombo.removeAllItems();

			Map<String, String> frameworkDependeices = SpringMVCPortletProjectConstants.springFrameworkDependeices;

			String dependency = frameworkDependeices.get((String)_frameworkDependenciesCombo.getSelectedItem());

			JSONArray viewTypeJSONArray = (JSONArray)dependencyObject.get(dependency);

			List<String> viewTypeList = new ArrayList<>();

			for (Object o : viewTypeJSONArray) {
				viewTypeList.add((String)o);
			}

			String[] viewTypes = new String[viewTypeList.size()];

			viewTypeList.toArray(viewTypes);

			_addComboItems(_getLables(viewTypes), _viewTypeCombo);

			_viewTypeCombo.setSelectedIndex(0);
		}
	}

	private SpringMVCPortletModuleBuilder _builder;
	private JComboBox<String> _frameworkCombo;
	private JComboBox<String> _frameworkDependenciesCombo;
	private String _liferayProductGroupVersion;
	private JComboBox<String> _liferayProductGroupVersionCombo;
	private JLabel _liferayVersionLabel;
	private JPanel _mainPanel;
	private JTextField _packageName;
	private final Project _project;
	private JComboBox<String> _viewTypeCombo;

}