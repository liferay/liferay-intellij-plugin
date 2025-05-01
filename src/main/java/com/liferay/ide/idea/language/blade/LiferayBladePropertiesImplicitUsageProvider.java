/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.blade;

import com.intellij.lang.properties.codeInspection.unused.ImplicitPropertyUsageProvider;
import com.intellij.lang.properties.psi.Property;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayBladePropertiesImplicitUsageProvider implements ImplicitPropertyUsageProvider {

	@Override
	public boolean isUsed(@NotNull Property property) {
		PsiFile containingFile = property.getContainingFile();

		String propertyKey = property.getKey();

		if ((containingFile != null) && (propertyKey != null)) {
			String fileName = containingFile.getName();

			return _BLADE_PROPERTIES.equals(fileName);
		}

		return false;
	}

	private static final String _BLADE_PROPERTIES = ".blade.properties";

}