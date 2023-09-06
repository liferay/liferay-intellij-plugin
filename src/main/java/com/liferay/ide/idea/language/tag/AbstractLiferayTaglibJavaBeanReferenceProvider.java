/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.tag;

import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.util.ClassUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public abstract class AbstractLiferayTaglibJavaBeanReferenceProvider extends PsiReferenceProvider {

	@NotNull
	@Override
	public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
		List<PsiReference> psiReferences = new ArrayList<>();

		String className = getClassName(element);

		if (className != null) {
			PsiManager psiManager = PsiManager.getInstance(element.getProject());

			PsiClass psiClass = ClassUtil.findPsiClass(psiManager, className);

			if (psiClass != null) {
				psiReferences.add(
					new LiferayTaglibJavaBeanReference(
						(XmlAttributeValue)element, ElementManipulators.getValueTextRange(element), psiClass));
			}
		}

		return psiReferences.toArray(new PsiReference[0]);
	}

	@Nullable
	protected abstract String getClassName(PsiElement psiElement);

}