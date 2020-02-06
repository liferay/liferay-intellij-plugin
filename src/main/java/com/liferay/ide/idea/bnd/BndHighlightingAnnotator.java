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

package com.liferay.ide.idea.bnd;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;

import com.liferay.ide.idea.bnd.parser.BndHeaderParser;
import com.liferay.ide.idea.bnd.parser.BndHeaderParsers;
import com.liferay.ide.idea.bnd.psi.AssignmentExpression;
import com.liferay.ide.idea.bnd.psi.Attribute;
import com.liferay.ide.idea.bnd.psi.BndHeader;
import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;
import com.liferay.ide.idea.bnd.psi.BndToken;
import com.liferay.ide.idea.bnd.psi.BndTokenType;
import com.liferay.ide.idea.bnd.psi.Clause;
import com.liferay.ide.idea.bnd.psi.Directive;

import org.jetbrains.annotations.NotNull;

/**
 * @author Charles Wu
 */
public class BndHighlightingAnnotator implements Annotator {

	@Override
	public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
		if (psiElement instanceof BndHeader) {
			BndHeader bndHeader = (BndHeader)psiElement;

			BndHeaderParser bndHeaderParser = BndHeaderParsers.parsersMap.get(bndHeader.getName());

			if (bndHeaderParser != null) {
				bndHeaderParser.annotate(bndHeader, annotationHolder);
			}
		}
		else if (psiElement instanceof BndHeaderValuePart) {
			PsiElement parentPsiElement = psiElement.getParent();

			if (parentPsiElement instanceof AssignmentExpression) {
				AssignmentExpression assignmentExpression = (AssignmentExpression)parentPsiElement;

				BndHeaderValuePart nameElement = assignmentExpression.getNameElement();

				if (parentPsiElement instanceof Attribute) {
					if (psiElement == nameElement) {
						_annotate(psiElement, OsgiManifestColorsAndFonts.ATTRIBUTE_NAME_KEY, annotationHolder);
					}
					else {
						_annotate(psiElement, OsgiManifestColorsAndFonts.ATTRIBUTE_VALUE_KEY, annotationHolder);
					}
				}
				else if (parentPsiElement instanceof Directive) {
					if (psiElement == nameElement) {
						_annotate(psiElement, OsgiManifestColorsAndFonts.DIRECTIVE_NAME_KEY, annotationHolder);
					}
					else {
						_annotate(psiElement, OsgiManifestColorsAndFonts.DIRECTIVE_VALUE_KEY, annotationHolder);
					}
				}
			}
		}
		else if (psiElement instanceof BndToken) {
			BndToken bndToken = (BndToken)psiElement;

			BndTokenType type = bndToken.getTokenType();

			if ((psiElement.getParent() instanceof Attribute) && (type == BndTokenType.EQUALS)) {
				_annotate(psiElement, OsgiManifestColorsAndFonts.ATTRIBUTE_ASSIGNMENT_KEY, annotationHolder);
			}
			else if ((psiElement.getParent() instanceof Directive) &&
					 ((type == BndTokenType.COLON) || (type == BndTokenType.EQUALS))) {

				_annotate(psiElement, OsgiManifestColorsAndFonts.DIRECTIVE_ASSIGNMENT_KEY, annotationHolder);
			}
			else if ((psiElement.getParent() instanceof Clause) && (type == BndTokenType.SEMICOLON)) {
				_annotate(psiElement, OsgiManifestColorsAndFonts.PARAMETER_SEPARATOR_KEY, annotationHolder);
			}
			else if ((psiElement.getParent() instanceof BndHeader) && (type == BndTokenType.COMMA)) {
				_annotate(psiElement, OsgiManifestColorsAndFonts.CLAUSE_SEPARATOR_KEY, annotationHolder);
			}
		}
	}

	private static void _annotate(
		PsiElement psiElement, TextAttributesKey textAttributesKey, AnnotationHolder annotationHolder) {

		if (psiElement != null) {
			Annotation annotation = annotationHolder.createInfoAnnotation(psiElement, null);

			annotation.setTextAttributes(textAttributesKey);
		}
	}

}