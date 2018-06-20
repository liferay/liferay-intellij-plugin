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

package com.liferay.ide.idea.terminal;

import com.intellij.ide.actions.ToggleDistractionFreeModeAction;
import com.intellij.ide.actions.ToggleToolbarAction;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.UISettingsListener;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.registry.RegistryValue;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.openapi.wm.impl.InternalDecorator;

/**
 * @author Terry Jia
 */
public class GogoShellToolWindowPanel extends SimpleToolWindowPanel implements UISettingsListener {

	public GogoShellToolWindowPanel(PropertiesComponent propertiesComponent, ToolWindow window) {
		super(false, true);

		_propertiesComponent = propertiesComponent;
		_window = window;
	}

	@Override
	public void addNotify() {
		super.addNotify();

		_updateDFState();
	}

	@Override
	public void uiSettingsChanged(UISettings uiSettings) {
		_updateDFState();
	}

	private static boolean _isDfmSupportEnabled() {
		RegistryValue value = Registry.get("terminal.distraction.free");

		return value.asBoolean();
	}

	private void _setDistractionFree(boolean distractionFree) {
		boolean visible = !distractionFree;

		_setToolbarVisible(visible);
		_setToolWindowHeaderVisible(visible);
	}

	private void _setToolbarVisible(boolean visible) {
		ToggleToolbarAction.setToolbarVisible(_window, _propertiesComponent, visible);
	}

	private void _setToolWindowHeaderVisible(boolean visible) {
		InternalDecorator decorator = ((ToolWindowEx)_window).getDecorator();

		decorator.setHeaderVisible(visible);
	}

	private boolean _shouldMakeDistractionFree() {
		ToolWindowAnchor anchor = _window.getAnchor();

		if (!anchor.isHorizontal() && ToggleDistractionFreeModeAction.isDistractionFreeModeEnabled()) {
			return true;
		}

		return false;
	}

	private void _updateDFState() {
		if (_isDfmSupportEnabled()) {
			_setDistractionFree(_shouldMakeDistractionFree());
		}
	}

	private final PropertiesComponent _propertiesComponent;
	private final ToolWindow _window;

}