/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.compoments;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * @author Charle Wu
 */
@SuppressWarnings("serial")
public class FixedSizeRefreshButton extends FixedSizeButton {

	public FixedSizeRefreshButton() {
		this(-1, null);
	}

	public FixedSizeRefreshButton(int size, JComponent component) {
		Icon icon = AllIcons.Actions.Refresh;

		if (icon != null) {
			setIcon(icon);
		}

		setAttachedComponent(component);
		setDefaultCapable(false);
		setFocusable(false);
		setMargin(JBUI.emptyInsets());
		setSize(size);

		if (UIUtil.isUnderIntelliJLaF() || _isDarkTheme()) {
			putClientProperty("JButton.buttonType", "square");
		}
	}

	private final boolean _isDarkTheme() {
		UIDefaults lookAndFeelDefaults = UIManager.getLookAndFeelDefaults();

		if ((lookAndFeelDefaults == null) || lookAndFeelDefaults.getBoolean("ui.theme.is.dark")) {
			return true;
		}

		return false;
	}

}