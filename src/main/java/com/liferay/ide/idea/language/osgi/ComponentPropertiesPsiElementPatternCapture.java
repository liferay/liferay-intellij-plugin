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
					PsiArrayInitializerMemberValue psiArrayInitializerMemberValue = PsiTreeUtil.getParentOfType(
						psiElement, PsiArrayInitializerMemberValue.class);

					if (psiArrayInitializerMemberValue == null) {
						return false;
					}

					return Stream.of(
						psiArrayInitializerMemberValue
					).map(
						array -> PsiTreeUtil.getParentOfType(array, PsiNameValuePair.class)
					).filter(
						Objects::nonNull
					).filter(
						pair -> {
							String name = pair.getName();

							return name.equals("property");
						}
					).map(
						pair -> PsiTreeUtil.getParentOfType(pair, PsiAnnotation.class)
					).filter(
						Objects::nonNull
					).map(
						PsiAnnotation::getQualifiedName
					).filter(
						qualifiedName -> "org.osgi.service.component.annotations.Component".equals(qualifiedName)
					).findFirst(
					).isPresent();
				}

			});
	}

}