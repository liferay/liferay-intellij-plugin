/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.core;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * @author Joye Luo
 */
public class LiferayIcons {

	public static final Icon BND_ICON = IconLoader.getIcon("/icons/bnd.png", LiferayIcons.class);

	public static final Icon LIFERAY_ICON = IconLoader.getIcon("/icons/liferay.svg", LiferayIcons.class);

	public static final Icon OSGI_ICON = IconLoader.getIcon("/icons/osgi.png", LiferayIcons.class);

	public static final Icon SPRING_ICON = IconLoader.getIcon(
		"/icons/portletMVC4Spring_16x16_new.png", LiferayIcons.class);

}