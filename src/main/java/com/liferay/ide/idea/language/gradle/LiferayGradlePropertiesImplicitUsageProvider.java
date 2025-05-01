/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.gradle;

import com.intellij.lang.properties.codeInspection.unused.ImplicitPropertyUsageProvider;
import com.intellij.lang.properties.psi.Property;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayGradlePropertiesImplicitUsageProvider implements ImplicitPropertyUsageProvider {

	@Override
	public boolean isUsed(@NotNull Property property) {
		PsiFile containingFile = property.getContainingFile();

		String propertyKey = property.getKey();

		if ((containingFile != null) && (propertyKey != null)) {
			String fileName = containingFile.getName();

			if (_GRADLE_PROPERTIES.equals(fileName) || _GRADLE_LOCAL_PROPERTIES.equals(fileName)) {
				return propertyKey.startsWith(_LIFERAY_WORKSPACE_PREFIX);
			}
		}

		return false;
	}

	private static final String _GRADLE_LOCAL_PROPERTIES = "gradle-local.properties";

	private static final String _GRADLE_PROPERTIES = "gradle.properties";

	private static final String _LIFERAY_WORKSPACE_PREFIX = "liferay.workspace.";

}