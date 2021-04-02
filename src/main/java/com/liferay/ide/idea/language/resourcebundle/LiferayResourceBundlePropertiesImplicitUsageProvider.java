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

package com.liferay.ide.idea.language.resourcebundle;

import com.intellij.codeInspection.unused.ImplicitPropertyUsageProvider;
import com.intellij.lang.properties.psi.Property;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayResourceBundlePropertiesImplicitUsageProvider
	implements com.intellij.lang.properties.codeInspection.unused.ImplicitPropertyUsageProvider {

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