/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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