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

package com.liferay.ide.idea.server;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.util.EnvVariablesTable;
import com.intellij.execution.util.EnvironmentVariable;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.table.TableView;
import com.intellij.util.EnvironmentUtil;
import com.intellij.util.io.IdeUtilIoBundle;
import com.intellij.util.ui.ListTableModel;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Simon Jiang
 */
public class LiferayEnvironmentVariablesDialog extends DialogWrapper {

	@Override
	public void doCancelAction() {
		if (getCancelAction().isEnabled()) {
			close(CANCEL_EXIT_CODE);
		}
	}

	@Override
	public void doCancelAction(AWTEvent source) {
		doCancelAction();
	}

	@Override
	public Dimension getInitialSize() {
		var size = super.getInitialSize();

		if (size != null) {
			return size;
		}

		return new Dimension(500, 500);
	}

	protected LiferayEnvironmentVariablesDialog(@NotNull LiferayEnvironmentVariablesTextFieldWithBrowseButton parent) {
		this(parent, false);
	}

	protected LiferayEnvironmentVariablesDialog(
		@NotNull LiferayEnvironmentVariablesTextFieldWithBrowseButton parent, boolean alwaysIncludeSystemVars) {

		super(parent, false);

		_myParent = parent;

		Map<String, String> userMap = new LinkedHashMap<>(_myParent.getEnvs());

		List<EnvironmentVariable> userList = LiferayEnvironmentVariablesTextFieldWithBrowseButton.convertToVariables(
			userMap, false);

		_myUserTable = createEnvVariablesTable(userList, true);

		JLabel label = new JLabel(ExecutionBundle.message("env.vars.user.title"));

		label.setLabelFor(
			_myUserTable.getTableView(
			).getComponent());

		_myWholePanel = new JPanel(new MigLayout("fill, ins 0, gap 0, hidemode 3"));

		_myWholePanel.add(label, "hmax pref, wrap");

		_myWholePanel.add(_myUserTable.getComponent(), "push, grow, wrap, gaptop 5");

		setTitle(ExecutionBundle.message("environment.variables.dialog.title"));
		init();
	}

	@NotNull
	@Override
	protected JComponent createCenterPanel() {
		return _myWholePanel;
	}

	@NotNull
	protected LiferayEnvironmentVariablesDialog.MyEnvVariablesTable createEnvVariablesTable(
		@NotNull List<EnvironmentVariable> variables, boolean userList) {

		return new LiferayEnvironmentVariablesDialog.MyEnvVariablesTable(variables, userList);
	}

	@Override
	protected void doOKAction() {
		_myUserTable.stopEditing();

		final Map<String, String> envMap = new LinkedHashMap<>();

		for (EnvironmentVariable variable : _myUserTable.getEnvironmentVariables()) {
			if (StringUtil.isEmpty(variable.getName()) && StringUtil.isEmpty(variable.getValue())) {
				continue;
			}

			envMap.put(variable.getName(), variable.getValue());
		}

		_myParent.setEnvs(envMap);
		_myParent.setPassParentEnvs(false);
		super.doOKAction();
	}

	@Nullable
	@Override
	protected ValidationInfo doValidate() {
		for (EnvironmentVariable variable : _myUserTable.getEnvironmentVariables()) {
			String name = variable.getName();
			String value = variable.getValue();

			if (StringUtil.isEmpty(name) && StringUtil.isEmpty(value))

				continue;

			if (!EnvironmentUtil.isValidName(name)) {
				return new ValidationInfo(IdeUtilIoBundle.message("run.configuration.invalid.env.name", name));
			}

			if (!EnvironmentUtil.isValidValue(value)) {
				return new ValidationInfo(IdeUtilIoBundle.message("run.configuration.invalid.env.value", name, value));
			}
		}

		return super.doValidate();
	}

	@Nullable
	@Override
	protected String getDimensionServiceKey() {
		return "LiferayEnvironmentVariablesDialog";
	}

	protected class MyEnvVariablesTable extends EnvVariablesTable {

		protected MyEnvVariablesTable(List<EnvironmentVariable> list, boolean userList) {
			myUserList = userList;

			TableView<EnvironmentVariable> tableView = getTableView();

			tableView.setVisibleRowCount(JBTable.PREFERRED_SCROLLABLE_VIEWPORT_HEIGHT_IN_ROWS);

			setValues(list);
			setPasteActionEnabled(myUserList);
		}

		@Nullable
		@Override
		protected AnActionButtonRunnable createAddAction() {
			return super.createAddAction();
		}

		@Override
		protected AnActionButton[] createExtraActions() {
			return super.createExtraActions();
		}

		@Override
		protected ListTableModel createListModel() {
			return new ListTableModel(
				new LiferayEnvironmentVariablesDialog.MyEnvVariablesTable.MyNameColumnInfo(),
				new LiferayEnvironmentVariablesDialog.MyEnvVariablesTable.MyValueColumnInfo());
		}

		@Nullable
		@Override
		protected AnActionButtonRunnable createRemoveAction() {
			return super.createRemoveAction();
		}

		protected final boolean myUserList;

		protected class MyNameColumnInfo extends NameColumnInfo {

			@Override
			public TableCellRenderer getCustomizedRenderer(EnvironmentVariable o, TableCellRenderer renderer) {
				if (o.getNameIsWriteable()) {
					return renderer;
				}

				return _myModifiedRenderer;
			}

			private final DefaultTableCellRenderer _myModifiedRenderer = new DefaultTableCellRenderer() {

				@Override
				public Component getTableCellRendererComponent(
					JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {

					Component component = super.getTableCellRendererComponent(
						table, value, selected, hasFocus, row, column);

					component.setEnabled(table.isEnabled() && (hasFocus || selected));

					return component;
				}

			};

		}

		protected class MyValueColumnInfo extends ValueColumnInfo {

			@Override
			public TableCellRenderer getCustomizedRenderer(EnvironmentVariable o, TableCellRenderer renderer) {
				return _myModifiedRenderer;
			}

			@Override
			public boolean isCellEditable(EnvironmentVariable environmentVariable) {
				return true;
			}

			private final DefaultTableCellRenderer _myModifiedRenderer = new DefaultTableCellRenderer() {

				@Override
				public Component getTableCellRendererComponent(
					JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {

					Component component = super.getTableCellRendererComponent(
						table, value, selected, hasFocus, row, column);

					component.setFont(
						component.getFont(
						).deriveFont(
							Font.TRUETYPE_FONT
						));

					return component;
				}

			};

		}

	}

	@NotNull
	private final LiferayEnvironmentVariablesTextFieldWithBrowseButton _myParent;

	@NotNull
	private final LiferayEnvironmentVariablesDialog.MyEnvVariablesTable _myUserTable;

	@NotNull
	private final JPanel _myWholePanel;

}