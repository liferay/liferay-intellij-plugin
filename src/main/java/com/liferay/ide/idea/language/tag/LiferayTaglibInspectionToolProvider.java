/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.tag;

import com.intellij.codeInspection.InspectionToolProvider;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
@SuppressWarnings("unchecked")
public class LiferayTaglibInspectionToolProvider implements InspectionToolProvider {

	@NotNull
	@Override
	public Class[] getInspectionClasses() {
		return new Class<?>[] {
			LiferayTaglibStrictQuoteEscapingInspection.class, LiferayTaglibStringConcatInspection.class
		};
	}

}