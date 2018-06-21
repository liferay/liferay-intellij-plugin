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
					PsiArrayInitializerMemberValue psiArrayInitializerMemberValue = PsiTreeUtil.getParentOfType(
						psiElement, PsiArrayInitializerMemberValue.class);

					if (psiArrayInitializerMemberValue != null) {
						PsiNameValuePair psiNameValuePair = PsiTreeUtil.getParentOfType(
							psiArrayInitializerMemberValue, PsiNameValuePair.class);

						if (psiNameValuePair != null) {
							String name = psiNameValuePair.getName();

							if ("property".equals(name)) {
								PsiAnnotation psiAnnotation = PsiTreeUtil.getParentOfType(
									psiNameValuePair, PsiAnnotation.class);

								if (psiAnnotation != null) {
									String qualifiedName = psiAnnotation.getQualifiedName();

									if ("org.osgi.service.component.annotations.Component".equals(qualifiedName)) {
										return true;
									}
								}
							}
						}
					}

					return false;
				}

			});
	}

}