/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.tag;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.references.PropertyReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibResourceBundlePropertyReference extends PropertyReference {

	public LiferayTaglibResourceBundlePropertyReference(
		@NotNull String key, @NotNull PsiElement element, @Nullable String bundleName, boolean soft) {

		super(key, element, bundleName, soft);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode) {
		Stream<ResolveResult> stream = Arrays.stream(super.multiResolve(incompleteCode));

		return stream.filter(
			resolveResult -> Stream.of(
				resolveResult
			).filter(
				result -> result instanceof PsiElementResolveResult
			).map(
				result -> {
					PsiElementResolveResult psiElementResolveResult = (PsiElementResolveResult)result;

					return psiElementResolveResult.getElement();
				}
			).filter(
				psiElement -> psiElement instanceof IProperty
			).map(
				psiElement -> {
					IProperty property = (IProperty)psiElement;

					return property.getPropertiesFile();
				}
			).anyMatch(
				LiferayTaglibResourceBundlePropertyReference::_isLanguageFile
			)
		).toArray(
			ResolveResult[]::new
		);
	}

	@Override
	protected void addKey(Object propertyObject, Set<Object> variants) {
		if (propertyObject instanceof IProperty) {
			IProperty property = (IProperty)propertyObject;

			if (_isLanguageFile(property.getPropertiesFile())) {

				// only add properties from Language files during code completion

				super.addKey(propertyObject, variants);
			}
		}
	}

	private static boolean _isLanguageFile(PropertiesFile propertiesFile) {
		if (propertiesFile != null) {
			String name = propertiesFile.getName();

			if (name != null) {
				return name.startsWith("Language");
			}
		}

		return false;
	}

}