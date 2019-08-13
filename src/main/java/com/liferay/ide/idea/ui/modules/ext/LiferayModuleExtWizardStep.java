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
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;

import com.liferay.ide.idea.ui.compoments.FixedSizeRefreshButton;
import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
 */
public class LiferayModuleExtWizardStep extends ModuleWizardStep {

	@SuppressWarnings("serial")
	public LiferayModuleExtWizardStep(WizardContext wizardContext, LiferayModuleExtBuilder liferayModuleExtBuilder) {
		_project = wizardContext.getProject();
		_liferayModuleExtBuilder = liferayModuleExtBuilder;

		_overrideFilesPanel.prepareRefreshButton(_refreshButton, false, () -> _insertOriginalModuleNames(true));

		_overrideFilesPanel.function = () -> {
			validate();

			return _getSelectedArtifact();
		};

		_overrideFilesPanel.setProject(_project);

		if (LiferayWorkspaceUtil.getIndexSources(_project)) {
			_indexSourcesLabel.setText("");
		}
		else {
			_indexSourcesLabel.setText(
				"This feature only works when the property \"target.platform.index.sources\" is set to true.");
		}

		_moduleNameHintLabel.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));

		// customize the presentation of a artifact

		_originalModuleNameComboBox.setRenderer(
			new ColoredListCellRenderer<LibraryData>() {

				@Override
				protected void customizeCellRenderer(
					@NotNull JList<? extends LibraryData> list, LibraryData value, int index, boolean selected,
					boolean hasFocus) {

					append(value.getArtifactId());
					append("  " + value.getVersion(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
				}

			});

		// only set the artifact name when select the value from list.

		_originalModuleNameComboBox.setEditor(
			new BasicComboBoxEditor() {

				@Override
				public void setItem(Object item) {
					if (item instanceof LibraryData) {
						LibraryData libraryData = (LibraryData)item;

						String text = libraryData.getArtifactId();

						if (!text.equals(editor.getText())) {
							editor.setText(text);
						}
					}
				}

			});

		// fill out the module version field automatic

		_originalModuleNameComboBox.addItemListener(
			event -> {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					Object item = event.getItem();

					if (item instanceof LibraryData) {
						LibraryData libraryData = (LibraryData)item;

						_originalModuleVersionField.setText(libraryData.getVersion());
					}
				}
			});

		KeyAdapter keyAdapter = new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e) {
				//reset the selected source files when input changed

				_overrideFilesPanel.listModel.clear();
			}

		};

		JTextField textField = (JTextField)_getEditor().getEditorComponent();

		textField.addKeyListener(keyAdapter);

		if (LiferayWorkspaceUtil.getTargetPlatformVersion(_project) != null) {
			_insertOriginalModuleNames(false);

			_originalModuleNameComboBox.setMaximumRowCount(12);
			_originalModuleVersionField.setEnabled(false);
		}
		else {
			_originalModuleVersionField.addKeyListener(keyAdapter);
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
		_liferayModuleExtBuilder.setOverrideFilesPanel(_overrideFilesPanel);
	}

	@Override
	public boolean validate() throws ConfigurationException {
		String validationTitle = "Validation Error";

		if (CoreUtil.isNullOrEmpty(_getOriginalModuleName())) {
			throw new ConfigurationException("Please input original module name", validationTitle);
		}
		else if ((LiferayWorkspaceUtil.getTargetPlatformVersion(_project) == null) &&
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

		return item.toString();
	}

	private LibraryData _getSelectedArtifact() {
		String originalModuleName = _getOriginalModuleName();

		for (LibraryData libraryData : _targetPlatformArtifacts) {
			if (originalModuleName.equals(libraryData.getArtifactId())) {
				return libraryData;
			}
		}

		return null;
	}

	private void _insertOriginalModuleNames(boolean clear) {
		_targetPlatformArtifacts = LiferayWorkspaceUtil.getTargetPlatformArtifacts(_project);

		if (clear) {
			try {
				_originalModuleNameComboBox.removeAllItems();
			}
			catch (NullPointerException npe) {
			}
		}

		_targetPlatformArtifacts.forEach(
			artifact -> {
				if (Objects.equals("com.liferay", artifact.getGroupId())) {
					_originalModuleNameComboBox.addItem(artifact);
				}
			});
	}

	private JLabel _indexSourcesLabel;
	private LiferayModuleExtBuilder _liferayModuleExtBuilder;
	private JPanel _mainPanel;
	private JLabel _moduleNameHintLabel;
	private JComboBox<LibraryData> _originalModuleNameComboBox;
	private JTextField _originalModuleVersionField;
	private OverrideFilesComponent _overrideFilesPanel;
	private final Project _project;
	private FixedSizeRefreshButton _refreshButton;
	private List<LibraryData> _targetPlatformArtifacts = Collections.emptyList();

}