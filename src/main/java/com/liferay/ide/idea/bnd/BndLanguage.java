/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd;

import com.intellij.lang.Language;
import com.intellij.openapi.util.NlsSafe;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class BndLanguage extends Language {

	public static final BndLanguage INSTANCE = new BndLanguage();

	public BndLanguage() {
		super("liferay-bnd");
	}

	@NlsSafe
	@NotNull
	@Override
	public String getDisplayName() {
		return "bnd (Liferay)";
	}

}