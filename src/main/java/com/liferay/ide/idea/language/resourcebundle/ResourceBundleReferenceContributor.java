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

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.position.FilterPattern;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class ResourceBundleReferenceContributor extends PsiReferenceContributor {

	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
		PsiElementPattern.Capture<PsiElement> psiElementCapture = PlatformPatterns.psiElement(PsiElement.class);

		registrar.registerReferenceProvider(
			psiElementCapture.and(new FilterPattern(new ResourceBundleReferenceElementFilter())),
			new ResourceBundleReferenceProvider());
	}

}