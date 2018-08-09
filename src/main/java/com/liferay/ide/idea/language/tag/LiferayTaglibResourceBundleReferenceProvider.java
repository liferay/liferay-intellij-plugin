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

import com.intellij.lang.properties.PropertiesReferenceProvider;
import com.intellij.lang.properties.references.PropertyReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.ProcessingContext;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibResourceBundleReferenceProvider extends PropertiesReferenceProvider {

	public LiferayTaglibResourceBundleReferenceProvider(boolean defaultSoft) {
		super(defaultSoft);
	}

	@NotNull
	@Override
	public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
		PsiReference[] referencesByElement = super.getReferencesByElement(element, context);

		if (referencesByElement.length == 1) {
			PsiReference psiReference = referencesByElement[0];

			if (psiReference instanceof PropertyReference) {
				PropertyReference propertyReference = (PropertyReference)psiReference;

				String canonicalText = propertyReference.getCanonicalText();
				PsiElement psiElement = propertyReference.getElement();
				boolean soft = propertyReference.isSoft();

				return new PsiReference[] {
					new LiferayTaglibResourceBundlePropertyReference(canonicalText, psiElement, null, soft)
				};
			}
		}

		return referencesByElement;
	}

}