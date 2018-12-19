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
		List<PsiReference> result = new ArrayList<>();

		String className = getClassName(element);

		if (className != null) {
			PsiManager psiManager = PsiManager.getInstance(element.getProject());

			PsiClass psiClass = ClassUtil.findPsiClass(psiManager, className);

			if (psiClass != null) {
				result.add(
					new LiferayTaglibJavaBeanReference(
						(XmlAttributeValue)element, ElementManipulators.getValueTextRange(element), psiClass));
			}
		}

		return result.toArray(new PsiReference[result.size()]);
	}

	@Nullable
	protected abstract String getClassName(PsiElement element);

}