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

package com.liferay.ide.idea.bndtools;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;

import com.liferay.ide.idea.bndtools.psi.AssignmentExpression;
import com.liferay.ide.idea.bndtools.psi.Attribute;
import com.liferay.ide.idea.bndtools.psi.Clause;
import com.liferay.ide.idea.bndtools.psi.Directive;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.jetbrains.lang.manifest.psi.ManifestToken;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;

/**
 * @author Charles Wu
 */
public class OsgiManifestHighlightingAnnotator implements Annotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (element instanceof HeaderValuePart) {
			PsiElement parent = element.getParent();

			if (parent instanceof AssignmentExpression) {
				HeaderValuePart nameElement = ((AssignmentExpression)parent).getNameElement();

				if (parent instanceof Attribute) {
					if (element == nameElement) {
						_annotate(element, OsgiManifestColorsAndFonts.ATTRIBUTE_NAME_KEY, holder);
					}
					else {
						_annotate(element, OsgiManifestColorsAndFonts.ATTRIBUTE_VALUE_KEY, holder);
					}
				}
				else if (parent instanceof Directive) {
					if (element == nameElement) {
						_annotate(element, OsgiManifestColorsAndFonts.DIRECTIVE_NAME_KEY, holder);
					}
					else {
						_annotate(element, OsgiManifestColorsAndFonts.DIRECTIVE_VALUE_KEY, holder);
					}
				}
			}
		}
		else if (element instanceof ManifestToken) {
			ManifestTokenType type = ((ManifestToken)element).getTokenType();

			if (element.getParent() instanceof Attribute && (type == ManifestTokenType.EQUALS)) {
				_annotate(element, OsgiManifestColorsAndFonts.ATTRIBUTE_ASSIGNMENT_KEY, holder);
			}
			else if (element.getParent() instanceof Directive &&
					 ((type == ManifestTokenType.COLON) || (type == ManifestTokenType.EQUALS))) {

				_annotate(element, OsgiManifestColorsAndFonts.DIRECTIVE_ASSIGNMENT_KEY, holder);
			}
			else if (element.getParent() instanceof Clause && (type == ManifestTokenType.SEMICOLON)) {
				_annotate(element, OsgiManifestColorsAndFonts.PARAMETER_SEPARATOR_KEY, holder);
			}
			else if (element.getParent() instanceof Header && (type == ManifestTokenType.COMMA)) {
				_annotate(element, OsgiManifestColorsAndFonts.CLAUSE_SEPARATOR_KEY, holder);
			}
		}
	}

	private static void _annotate(PsiElement element, TextAttributesKey key, AnnotationHolder holder) {
		if (element != null) {
			Annotation annotation = holder.createInfoAnnotation(element, null);

			annotation.setTextAttributes(key);
		}
	}

}