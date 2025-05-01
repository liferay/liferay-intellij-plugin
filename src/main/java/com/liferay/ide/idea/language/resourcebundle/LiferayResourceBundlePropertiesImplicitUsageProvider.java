/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.resourcebundle;

import com.intellij.lang.properties.codeInspection.unused.ImplicitPropertyUsageProvider;
import com.intellij.lang.properties.psi.Property;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayResourceBundlePropertiesImplicitUsageProvider implements ImplicitPropertyUsageProvider {

	@Override
	public boolean isUsed(@NotNull Property property) {
		PsiFile containingFile = property.getContainingFile();

		String propertyKey = property.getKey();

		if ((containingFile != null) && (propertyKey != null)) {
			String fileName = containingFile.getName();

			if (fileName.startsWith(_LANGUAGE_PROPERTY_FILE_PREFIX)) {
				for (String implicitPropertyPrefix : _IMPLICIT_PROPERTY_PREFIXES) {
					if (propertyKey.startsWith(implicitPropertyPrefix)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private static final String[] _IMPLICIT_PROPERTY_PREFIXES = {
		"action.", "category.", "javax.portlet.description.", "javax.portlet.long-title.", "javax.portlet.title.",
		"model.resource."
	};

	private static final String _LANGUAGE_PROPERTY_FILE_PREFIX = "Language";

}