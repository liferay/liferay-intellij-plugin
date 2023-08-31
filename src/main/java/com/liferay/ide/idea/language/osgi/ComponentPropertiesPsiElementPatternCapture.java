/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.osgi;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class ComponentPropertiesPsiElementPatternCapture {

	public static PsiElementPattern.Capture<PsiElement> instance;

	static {
		instance = PlatformPatterns.psiElement();
		instance = instance.inside(PsiJavaPatterns.literalExpression());
		instance = instance.with(
			new PatternCondition<PsiElement>("pattern") {

				@Override
				public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
					PsiNameValuePair psiNameValuePair;

					PsiArrayInitializerMemberValue psiArrayInitializerMemberValue = PsiTreeUtil.getParentOfType(
						psiElement, PsiArrayInitializerMemberValue.class);

					if (psiArrayInitializerMemberValue != null) {
						psiNameValuePair = PsiTreeUtil.getParentOfType(
							psiArrayInitializerMemberValue, PsiNameValuePair.class);
					}
					else {
						psiNameValuePair = PsiTreeUtil.getParentOfType(psiElement, PsiNameValuePair.class);
					}

					return Stream.of(
						psiNameValuePair
					).filter(
						Objects::nonNull
					).filter(
						pair -> {
							String name = pair.getName();

							if (name != null) {
								return Objects.equals(name, "property");
							}

							return false;
						}
					).map(
						pair -> PsiTreeUtil.getParentOfType(pair, PsiAnnotation.class)
					).filter(
						Objects::nonNull
					).map(
						PsiAnnotation::getQualifiedName
					).anyMatch(
						"org.osgi.service.component.annotations.Component"::equals
					);
				}

			});
	}

}