/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.tag;

import com.intellij.codeInsight.lookup.LookupElementBuilder;

import com.liferay.ide.idea.core.LiferayIcons;

/**
 * @author Terry Jia
 */
public class LiferayLookupElementBuilderFactory {

	public static LookupElementBuilder create(String value, String type) {
		return LookupElementBuilder.create(
			value
		).withTypeText(
			type
		).withIcon(
			LiferayIcons.LIFERAY_ICON
		);
	}

}