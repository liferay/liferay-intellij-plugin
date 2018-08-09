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
		ResolveResult[] resolveResults = super.multiResolve(incompleteCode);

		Stream<ResolveResult> stream = Arrays.stream(resolveResults);

		return stream.filter(
			element -> {
				if (element instanceof PsiElementResolveResult) {
					PsiElementResolveResult psiElementResolveResult = (PsiElementResolveResult)element;

					PsiElement psiElement = psiElementResolveResult.getElement();

					if (psiElement instanceof IProperty) {
						IProperty property = (IProperty)psiElement;

						PropertiesFile propertiesFile = property.getPropertiesFile();

						//only resolve properties from Language files
						return _isLanguageFile(propertiesFile);
					}
				}

				return false;
			}).toArray(ResolveResult[]::new);
	}

	@Override
	protected void addKey(Object propertyObject, Set<Object> variants) {
		if (propertyObject instanceof IProperty) {
			IProperty property = (IProperty)propertyObject;

			PropertiesFile propertiesFile = property.getPropertiesFile();

			if (_isLanguageFile(propertiesFile)) {
				//only add properties from Language files during code completion
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