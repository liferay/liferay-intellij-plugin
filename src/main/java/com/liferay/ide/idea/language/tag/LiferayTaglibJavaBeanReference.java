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

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiFormatUtil;
import com.intellij.psi.util.PsiFormatUtilBase;
import com.intellij.psi.xml.XmlAttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibJavaBeanReference extends PsiReferenceBase<XmlAttributeValue> {

	public LiferayTaglibJavaBeanReference(
		XmlAttributeValue xmlAttributeValue, TextRange rangeInElement, PsiClass targetClass) {

		super(xmlAttributeValue, rangeInElement, true);

		_targetClass = targetClass;
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		List<Object> result = new ArrayList<>();

		Map<String, PsiMethod> allProperties = PropertyUtil.getAllProperties(_targetClass, false, true, true);

		for (PsiMethod psiMethod : allProperties.values()) {
			String name = PropertyUtil.getPropertyNameByGetter(psiMethod);

			PsiType psiType = psiMethod.getReturnType();

			PsiType psiSubstitutor = PsiSubstitutor.EMPTY.substitute(psiType);

			result.add(
				LookupElementBuilder.create(
					name
				).withIcon(
					psiMethod.getIcon(Iconable.ICON_FLAG_VISIBILITY)
				).withTailText(
					" (" + PsiFormatUtil.formatMethod(psiMethod, PsiSubstitutor.EMPTY, _FORMAT_METHOD_OPTIONS, 0) + ")",
					true
				).withTypeText(
					psiSubstitutor.getPresentableText()
				));
		}

		return result.toArray(new Object[0]);
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		return PropertyUtil.findPropertyGetter(_targetClass, getValue(), false, true);
	}

	private static final int _FORMAT_METHOD_OPTIONS =
		PsiFormatUtilBase.SHOW_CONTAINING_CLASS | PsiFormatUtilBase.SHOW_FQ_NAME | PsiFormatUtilBase.SHOW_NAME |
		PsiFormatUtilBase.SHOW_PARAMETERS;

	private PsiClass _targetClass;

}