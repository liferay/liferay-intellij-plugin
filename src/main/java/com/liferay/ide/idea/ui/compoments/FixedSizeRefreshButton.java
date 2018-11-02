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

package com.liferay.ide.idea.ui.compoments;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * @author Charle Wu
 */
public class FixedSizeRefreshButton extends FixedSizeButton {

	public FixedSizeRefreshButton() {
		this(-1, null);
	}

	public FixedSizeRefreshButton(int size, JComponent component) {
		Icon icon = AllIcons.Actions.Refresh;

		if (icon != null) {
			setIcon(icon);
		}

		setSize(size);
		setAttachedComponent(component);
		setMargin(JBUI.emptyInsets());
		setDefaultCapable(false);
		setFocusable(false);

		if ((UIUtil.isUnderAquaLookAndFeel() && (size == -1)) || UIUtil.isUnderIntelliJLaF() ||
			UIUtil.isUnderDarcula()) {

			putClientProperty("JButton.buttonType", "square");
		}
	}

}