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

package com.liferay.ide.idea.bnd.parser;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiClass;

import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;
import com.liferay.ide.idea.bnd.psi.util.BndPsiUtil;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class BundleActivatorParser extends ClassReferenceParser {

	public static final BundleActivatorParser INSTANCE = new BundleActivatorParser();

	@Override
	protected boolean checkClass(
		@NotNull BndHeaderValuePart bndHeaderValuePart, @NotNull PsiClass psiClass,
		@NotNull AnnotationHolder annotationHolder) {

		boolean result = super.checkClass(bndHeaderValuePart, psiClass, annotationHolder);

		PsiClass bundleActivatorClass = BndPsiUtil.getBundleActivatorClass(bndHeaderValuePart);

		if ((bundleActivatorClass != null) && !psiClass.isInheritor(bundleActivatorClass, true)) {
			annotationHolder.createErrorAnnotation(
				bndHeaderValuePart.getHighlightingRange(), "Activator class does not inherit from BundleActivator");

			return true;
		}

		return result;
	}

	private BundleActivatorParser() {
	}

}