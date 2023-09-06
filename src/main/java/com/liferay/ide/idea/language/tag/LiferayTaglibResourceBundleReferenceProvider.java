/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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