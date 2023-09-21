/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import com.intellij.ide.util.projectWizard.EmptyModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.templates.TemplateModuleBuilder;
import com.intellij.projectImport.ProjectFormatPanel;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class LiferayProjectSettingsStep extends ModuleWizardStep {

	public static void addField(String label, JComponent field, JPanel panel) {
		JLabel jLabel = new JBLabel(label);

		jLabel.setLabelFor(field);

		GridBagConstraints gridBagConstraints = new GridBagConstraints(
			0, GridBagConstraints.RELATIVE, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			JBUI.insetsBottom(5), 4, 0);

		panel.add(jLabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints(
			1, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			JBUI.insetsBottom(5), 0, 0);

		panel.add(field, gridBagConstraints);
	}

	public LiferayProjectSettingsStep(LiferayModuleBuilder liferayModuleBuilder, WizardContext context) {
		_context = context;

		_initProjectSettingsStep(context);
	}

	public LiferayProjectSettingsStep(WizardContext context) {
		_context = context;

		_initProjectSettingsStep(context);

		_liferayProjectTypesComponent.hideComponent();
	}

	@Override
	public void _init() {
		JTextField moduleNameField = _moduleNameLocationComponent.getModuleNameField();

		if (StringUtil.isNotEmpty(moduleNameField.getText())) {
			_moduleNameLocationComponent.updateLocations(moduleNameField.getText());
		}
		else {
			_moduleNameLocationComponent.updateLocations();
		}
	}

	public void createUIComponents() {
		_moduleNameLocationComponent = new LiferayModuleNameLocationComponent(_context);
		_liferayProjectTypesComponent = new LiferayProjectTypesComponent(_context);
	}

	@Override
	public JComponent getComponent() {
		return _mainPanel;
	}

	@Override
	public String getHelpId() {
		if (_context.isCreatingNewProject()) {
			return "New_Project_Main_Settings";
		}

		return "Add_Module_Main_Settings";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return "Project Settings";
	}

	@Override
	public JComponent getPreferredFocusedComponent() {
		return _getNameComponent();
	}

	@Override
	public void updateDataModel() {
		_context.setProjectName(_namePathComponent.getNameValue());
		_context.setProjectFileDirectory(_namePathComponent.getPath());

		_formatPanel.updateData(_context);
		_moduleNameLocationComponent.updateDataModel();

		ProjectBuilder moduleBuilder = _context.getProjectBuilder();

		if (moduleBuilder instanceof TemplateModuleBuilder) {
			_context.setProjectStorageFormat(StorageScheme.DIRECTORY_BASED);
		}
		else if (moduleBuilder instanceof LiferayModuleBuilder) {
			_liferayProjectTypesComponent.updateDataModel();
		}
	}

	@Override
	public void updateStep() {
		_setupPanels();
	}

	@Override
	public boolean validate() throws ConfigurationException {
		if (_context.isCreatingNewProject() &&
			!_namePathComponent.validateNameAndPath(_context, _formatPanel.isDefault())) {

			return false;
		}

		if (!_moduleNameLocationComponent.validate()) {
			return false;
		}

		if ((_context.getProjectBuilder() instanceof LiferayModuleBuilder) &&
			!_liferayProjectTypesComponent.validateComponent()) {

			return false;
		}

		return true;
	}

	private void _addProjectFormat(JPanel panel) {
		addField("Project \u001bformat:", _formatPanel.getStorageFormatComboBox(), panel);
	}

	private JPanel _getModulePanel() {
		return _moduleNameLocationComponent.getModulePanel();
	}

	private JTextField _getNameComponent() {
		if (_context.isCreatingNewProject()) {
			return _namePathComponent.getNameComponent();
		}

		return _moduleNameLocationComponent.getModuleNameField();
	}

	private void _initProjectSettingsStep(WizardContext context) {
		_formatPanel = new ProjectFormatPanel();

		_namePathComponent = LiferayNamePathComponent.initNamePathComponent(context);

		_namePathComponent.setShouldBeAbsolute(true);

		JPanel modulePanel = _getModulePanel();

		if (context.isCreatingNewProject()) {
			_settingsPanel.add(_namePathComponent, BorderLayout.NORTH);
		}
		else {
			_settingsPanel.add(modulePanel, BorderLayout.NORTH);
		}

		_moduleNameLocationComponent.bindModuleSettings(_namePathComponent);

		if (_context.isCreatingNewProject()) {
			_addProjectFormat(modulePanel);
		}

		_liferayProjectTypesComponent.initProjectTypeComponent(_moduleNameLocationComponent, _context);
	}

	private void _restorePanel(JPanel component, int i) {
		while (component.getComponentCount() > i) {
			component.remove(component.getComponentCount() - 1);
		}
	}

	private void _setupPanels() {
		ModuleBuilder moduleBuilder = (ModuleBuilder)_context.getProjectBuilder();

		_restorePanel(_namePathComponent, 4);
		_restorePanel(_getModulePanel(), _context.isCreatingNewProject() ? 8 : 6);

		for (int i = 0; i < 6; i++) {
			Component component = _getModulePanel().getComponent(i);

			component.setVisible(!(moduleBuilder instanceof EmptyModuleBuilder));
		}

		_settingsPanel.revalidate();
		_settingsPanel.repaint();
	}

	private WizardContext _context;
	private ProjectFormatPanel _formatPanel;
	private LiferayProjectTypesComponent _liferayProjectTypesComponent;
	private JPanel _mainPanel;
	private LiferayModuleNameLocationComponent _moduleNameLocationComponent;
	private LiferayNamePathComponent _namePathComponent;
	private JPanel _settingsPanel;

}