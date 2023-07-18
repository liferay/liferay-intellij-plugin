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
import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.execution.util.EnvVariablesTable;
import com.intellij.execution.util.EnvironmentVariable;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.UserActivityProviderComponent;
import com.intellij.util.containers.ContainerUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import org.jetbrains.annotations.NotNull;

/**
 * @author Simon Jiang
 */
public class LiferayEnvironmentVariablesTextFieldWithBrowseButton
	extends TextFieldWithBrowseButton.NoPathCompletion implements UserActivityProviderComponent {

	public LiferayEnvironmentVariablesTextFieldWithBrowseButton() {
		myData = EnvironmentVariablesData.create(Map.of(), false, null);
		addActionListener(
			new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					setEnvs(EnvVariablesTable.parseEnvsFromText(getText()));
					createLiferayDialog().show();
				}

			});

		getTextField(
		).getDocument(
		).addDocumentListener(
			new DocumentAdapter() {

				@Override
				protected void textChanged(@NotNull DocumentEvent e) {
					if (!StringUtil.equals(stringifyEnvs(myData), getText())) {
						Map<String, String> textEnvMap = EnvVariablesTable.parseEnvsFromText(getText());

						myData = myData.with(textEnvMap);

						_fireStateChanged();
					}
				}

			}
		);

		getTextField().setEditable(false);
	}

	@Override
	public void addChangeListener(@NotNull ChangeListener changeListener) {
		_myListeners.add(changeListener);
	}

	@NotNull
	public EnvironmentVariablesData getData() {
		return myData;
	}

	/**
	 * @return unmodifiable Map instance
	 */
	@NotNull
	public Map<String, String> getEnvs() {
		return myData.getEnvs();
	}

	public boolean isPassParentEnvs() {
		return myData.isPassParentEnvs();
	}

	@Override
	public void removeChangeListener(@NotNull ChangeListener changeListener) {
		_myListeners.remove(changeListener);
	}

	public void setData(@NotNull EnvironmentVariablesData data) {
		EnvironmentVariablesData oldData = myData;
		myData = data;
		setText(stringifyEnvs(data));

		if (!oldData.equals(data)) {
			_fireStateChanged();
		}
	}

	/**
	 * @param envMap Map instance containing user-defined environment variables
	 *             (iteration order should be reliable user-specified, like {@link LinkedHashMap} or {@link ImmutableMap})
	 */
	public void setEnvs(@NotNull Map<String, String> envMap) {
		setData(myData.with(envMap));
	}

	public void setPassParentEnvs(boolean passParentEnvs) {
		setData(myData.with(passParentEnvs));
	}

	protected static List<EnvironmentVariable> convertToVariables(Map<String, String> map, final boolean readOnly) {
		return ContainerUtil.map(
			map.entrySet(),
			entry -> new EnvironmentVariable(entry.getKey(), entry.getValue(), readOnly) {

				@Override
				public boolean getNameIsWriteable() {
					return !readOnly;
				}

			});
	}

	protected LiferayEnvironmentVariablesDialog createLiferayDialog() {
		return new LiferayEnvironmentVariablesDialog(this);
	}

	@NotNull
	@Override
	protected Icon getDefaultIcon() {
		return AllIcons.General.InlineVariables;
	}

	@NotNull
	@Override
	protected Icon getHoveredIcon() {
		return AllIcons.General.InlineVariablesHover;
	}

	@NlsContexts.Tooltip
	@NotNull
	@Override
	protected String getIconTooltip() {
		return ExecutionBundle.message("specify.environment.variables.tooltip") + " (" +
			KeymapUtil.getKeystrokeText(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK)) + ")";
	}

	protected boolean isModifiedSysEnv(@NotNull EnvironmentVariable v) {
		if (!v.getNameIsWriteable() && !Objects.equals(v.getValue(), myParentDefaults.get(v.getName()))) {
			return true;
		}

		return false;
	}

	@NotNull
	protected String stringifyEnvs(@NotNull EnvironmentVariablesData evd) {
		if (evd.getEnvs(
			).isEmpty()) {

			return "";
		}

		StringBuilder buf = new StringBuilder();

		for (Map.Entry<String, String> entry :
				evd.getEnvs(
				).entrySet()) {

			if (buf.length() > 0) {
				buf.append(";");
			}

			buf.append(
				StringUtil.escapeChar(entry.getKey(), ';')
			).append(
				"="
			).append(
				StringUtil.escapeChar(entry.getValue(), ';')
			);
		}

		return buf.toString();
	}

	protected EnvironmentVariablesData myData;
	protected final Map<String, String> myParentDefaults = new LinkedHashMap<>();

	private void _fireStateChanged() {
		for (ChangeListener listener : _myListeners) {
			listener.stateChanged(new ChangeEvent(this));
		}
	}

	private final List<ChangeListener> _myListeners = ContainerUtil.createLockFreeCopyOnWriteList();

}