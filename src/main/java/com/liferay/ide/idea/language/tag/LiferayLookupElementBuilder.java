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

package com.liferay.ide.idea.language.tag;

import com.intellij.codeInsight.lookup.LookupElementBuilder;

import icons.LiferayIcons;

/**
 * @author Terry Jia
 */
public class LiferayLookupElementBuilder {

	public static LookupElementBuilder create(String value, String type) {
		LookupElementBuilder builder = LookupElementBuilder.create(value);

		builder = builder.withTypeText(type);
		builder = builder.withIcon(LiferayIcons.LIFERAY_ICON);

		return builder;
	}

}