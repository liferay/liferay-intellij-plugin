/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import com.intellij.platform.templates.BuilderBasedTemplate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Joye Luo
 */
public class LiferayWorkspaceTemplate extends BuilderBasedTemplate {

	public LiferayWorkspaceTemplate(String name, String description, LiferayWorkspaceBuilder builder) {
		super(builder);

		_name = name;
		_description = description;
	}

	@Nullable
	@Override
	public String getDescription() {
		return _description;
	}

	@NotNull
	@Override
	public String getName() {
		return _name;
	}

	private final String _description;
	private final String _name;

}