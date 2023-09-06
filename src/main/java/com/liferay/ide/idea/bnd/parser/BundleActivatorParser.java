/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.parser;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiClass;

import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;
import com.liferay.ide.idea.bnd.psi.util.BndPsiUtil;
import com.liferay.ide.idea.util.LiferayAnnotationUtil;

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
			LiferayAnnotationUtil.createAnnotation(
				annotationHolder, HighlightSeverity.ERROR, "Activator class does not inherit from BundleActivator",
				bndHeaderValuePart.getHighlightingRange());

			return true;
		}

		return result;
	}

	private BundleActivatorParser() {
	}

}