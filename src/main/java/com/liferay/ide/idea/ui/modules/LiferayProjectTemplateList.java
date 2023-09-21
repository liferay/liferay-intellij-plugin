/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.ListItemDescriptorAdapter;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.templates.ArchivedProjectTemplate;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.popup.list.GroupedItemsListRenderer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;

import java.awt.BorderLayout;

import java.io.Serial;

import java.util.List;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 */
public class LiferayProjectTemplateList extends JPanel {

	public LiferayProjectTemplateList() {
		super(new BorderLayout());

		add(_mainPanel, BorderLayout.CENTER);

		ListItemDescriptorAdapter<ProjectTemplate> descriptor = new ListItemDescriptorAdapter<>() {

			@Nullable
			@Override
			public Icon getIconFor(ProjectTemplate value) {
				return value.getIcon();
			}

			@NotNull
			@Override
			public String getTextFor(ProjectTemplate value) {
				return value.getName();
			}

		};

		GroupedItemsListRenderer<ProjectTemplate> renderer = new GroupedItemsListRenderer<>(descriptor) {

			@Override
			protected void customizeComponent(
				JList<? extends ProjectTemplate> list, ProjectTemplate value, boolean selected) {

				super.customizeComponent(list, value, selected);

				Icon icon = myTextLabel.getIcon();

				if ((icon != null) && (myTextLabel.getDisabledIcon() == icon)) {
					myTextLabel.setDisabledIcon(IconLoader.getDisabledIcon(icon));
				}

				myTextLabel.setEnabled(_templateList.isEnabled());
				myTextLabel.setBorder(JBUI.Borders.empty(3));
			}

		};

		_templateList.setCellRenderer(renderer);

		ListSelectionModel listSelectionModel = _templateList.getSelectionModel();

		listSelectionModel.addListSelectionListener(event -> _updateSelection());

		Messages.installHyperlinkSupport(_description);
	}

	public void addListSelectionListener(ListSelectionListener listener) {
		_templateList.addListSelectionListener(listener);
	}

	@Nullable
	public ProjectTemplate getSelectedTemplate() {
		return _templateList.getSelectedValue();
	}

	public void restoreSelection() {
		PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();

		String templateName = propertiesComponent.getValue(_PROJECT_WIZARD_TEMPLATE);

		if ((templateName != null) && (_templateList.getModel() instanceof CollectionListModel)) {
			CollectionListModel<ProjectTemplate> collectionListModel =
				(CollectionListModel<ProjectTemplate>)_templateList.getModel();

			List<ProjectTemplate> list = collectionListModel.toList();

			ProjectTemplate template = ContainerUtil.find(list, template1 -> templateName.equals(template1.getName()));

			if (template != null) {
				_templateList.setSelectedValue(template, true);
			}
		}

		ListSelectionModel listSelectionModel = _templateList.getSelectionModel();

		listSelectionModel.addListSelectionListener(
			new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent event) {
					ProjectTemplate template = getSelectedTemplate();

					if (template != null) {
						PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();

						propertiesComponent.setValue(_PROJECT_WIZARD_TEMPLATE, template.getName());
					}
				}

			});
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		_templateList.setEnabled(enabled);

		if (!enabled) {
			_templateList.clearSelection();
		}
		else {
			_templateList.setSelectedIndex(0);
		}

		_description.setEnabled(enabled);
	}

	public void setTemplates(List<ProjectTemplate> list, boolean preserveSelection) {
		list.sort(
			(o1, o2) -> Boolean.compare(o1 instanceof ArchivedProjectTemplate, o2 instanceof ArchivedProjectTemplate));

		int index = preserveSelection ? _templateList.getSelectedIndex() : -1;

		_templateList.setModel(new CollectionListModel<>(list));

		if (_templateList.isEnabled()) {
			_templateList.setSelectedIndex((index == -1) ? 0 : index);
		}

		_updateSelection();
	}

	private void _updateSelection() {
		_description.setText("");

		ProjectTemplate template = getSelectedTemplate();

		if (template != null) {
			String description = template.getDescription();

			if (StringUtil.isNotEmpty(description)) {
				description =
					"<html><body><font " + (SystemInfo.isMac ? "" : "face=\"Verdana\" size=\"-1\"") + '>' +
						description + "</font></body></html>";

				_description.setText(description);
			}
		}
	}

	private static final String _PROJECT_WIZARD_TEMPLATE = "project.wizard.template";

	@Serial
	private static final long serialVersionUID = 7608936525034551298L;

	private JTextPane _description;
	private JPanel _mainPanel;
	private JBList<ProjectTemplate> _templateList;

}