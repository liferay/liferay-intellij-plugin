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

package com.liferay.ide.idea.ui.modules.ext;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.util.ui.UIUtil;

import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.awt.event.ItemEvent;

import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.jetbrains.annotations.NotNull;

/**
 * @author Charles Wu
 * @author Terry Jia
 */
public class LiferayModuleExtWizardStep extends ModuleWizardStep implements LiferayWorkspaceSupport {

	public LiferayModuleExtWizardStep(WizardContext wizardContext, LiferayModuleExtBuilder liferayModuleExtBuilder) {
		_project = wizardContext.getProject();
		_liferayModuleExtBuilder = liferayModuleExtBuilder;

		_moduleNameHintLabel.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));

		// customize the presentation of a artifact

		_originalModuleNameComboBox.setRenderer(
			new ColoredListCellRenderer<String>() {

				@Override
				protected void customizeCellRenderer(
					@NotNull JList<? extends String> list, String value, int index, boolean selected,
					boolean hasFocus) {

					append(value);
				}

			});

		// only set the artifact name when select the value from list.

		_originalModuleNameComboBox.setEditor(
			new BasicComboBoxEditor() {

				@Override
				public void setItem(Object item) {
					String text = (String)item;

					editor.setText(text);
				}

			});

		// fill out the module version field automatic

		_originalModuleNameComboBox.addItemListener(
			event -> {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					Object item = event.getItem();

					String dependency = (String)item;

					String[] s = dependency.split(" ");

					if (s.length == 2) {
						_originalModuleVersionField.setText(s[1]);
					}
				}
			});

		if ((_project != null) && (getTargetPlatformVersion(_project) != null)) {
			_insertOriginalModuleNames();

			_originalModuleNameComboBox.setMaximumRowCount(12);
			_originalModuleVersionField.setEnabled(false);
		}
	}

	@Override
	public JComponent getComponent() {
		return _mainPanel;
	}

	@Override
	public void updateDataModel() {
		_liferayModuleExtBuilder.setOriginalModuleName(_getOriginalModuleName());
		_liferayModuleExtBuilder.setOriginalModuleVersion(_originalModuleVersionField.getText());
	}

	@Override
	public boolean validate() throws ConfigurationException {
		String validationTitle = "Validation Error";

		if (CoreUtil.isNullOrEmpty(_getOriginalModuleName())) {
			throw new ConfigurationException("Please input original module name", validationTitle);
		}
		else if ((getTargetPlatformVersion(_project) == null) &&
				 CoreUtil.isNullOrEmpty(_originalModuleVersionField.getText())) {

			throw new ConfigurationException("Please input original module version", validationTitle);
		}

		return true;
	}

	private ComboBoxEditor _getEditor() {
		return _originalModuleNameComboBox.getEditor();
	}

	private String _getOriginalModuleName() {
		Object item = _getEditor().getItem();

		String s = (String)item;

		int i1 = s.indexOf(":");
		int i2 = s.indexOf(" ");

		if ((i1 > -1) && (i2 > -1)) {
			return s.substring(i1 + 1, i2);
		}

		return s;
	}

	private void _insertOriginalModuleNames() {
		List<String> targetPlatformArtifacts = getTargetPlatformDependencies(_project);

		targetPlatformArtifacts.forEach(
			line -> {
				String[] s = line.split(":");

				if (s[0].equals("com.liferay")) {
					_originalModuleNameComboBox.addItem(line);
				}
			});
	}

	private LiferayModuleExtBuilder _liferayModuleExtBuilder;
	private JPanel _mainPanel;
	private JLabel _moduleNameHintLabel;
	private JComboBox<String> _originalModuleNameComboBox;
	private JTextField _originalModuleVersionField;
	private final Project _project;

}