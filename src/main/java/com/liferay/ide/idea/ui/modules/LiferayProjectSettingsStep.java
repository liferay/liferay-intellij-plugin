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

package com.liferay.ide.idea.ui.modules;

import com.intellij.ide.util.newProjectWizard.SelectTemplateSettings;
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
import com.intellij.ui.HideableDecorator;
import com.intellij.ui.IdeBorderFactory;
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

import org.jetbrains.annotations.Nullable;

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
	public void onStepLeaving() {
		SelectTemplateSettings selectTemplateSettings = SelectTemplateSettings.getInstance();

		selectTemplateSettings.EXPERT_MODE = _expertDecorator.isExpanded();
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

		if (_settingsStep != null) {
			_settingsStep.updateDataModel();
		}
	}

	@Override
	public void updateStep() {
		SelectTemplateSettings selectTemplateSettings = SelectTemplateSettings.getInstance();

		_expertDecorator.setOn(selectTemplateSettings.EXPERT_MODE);

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

		if (_settingsStep != null) {
			return _settingsStep.validate();
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

		_expertDecorator = new HideableDecorator(_expertPlaceholder, "Mor&e Settings", false);
		_expertPanel.setBorder(JBUI.Borders.empty(0, IdeBorderFactory.TITLED_BORDER_INDENT, 5, 0));

		_expertDecorator.setContentComponent(_expertPanel);

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
		_restorePanel(_expertPanel, _context.isCreatingNewProject() ? 1 : 0);

		_expertPlaceholder.setVisible(
			!(moduleBuilder instanceof TemplateModuleBuilder) && (_expertPanel.getComponentCount() > 0));

		for (int i = 0; i < 6; i++) {
			Component component = _getModulePanel().getComponent(i);

			component.setVisible(!(moduleBuilder instanceof EmptyModuleBuilder));
		}

		_settingsPanel.revalidate();
		_settingsPanel.repaint();
	}

	private WizardContext _context;
	private HideableDecorator _expertDecorator;
	private JPanel _expertPanel;
	private JPanel _expertPlaceholder;
	private ProjectFormatPanel _formatPanel;
	private LiferayProjectTypesComponent _liferayProjectTypesComponent;
	private JPanel _mainPanel;
	private LiferayModuleNameLocationComponent _moduleNameLocationComponent;
	private LiferayNamePathComponent _namePathComponent;
	private JPanel _settingsPanel;

	@Nullable
	private ModuleWizardStep _settingsStep;

}