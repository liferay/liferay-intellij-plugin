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

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import org.jetbrains.lang.manifest.highlighting.ManifestColorsAndFonts;

/**
 * @author Charles Wu
 */
public class OsgiManifestColorsAndFonts {

	public static final TextAttributesKey ATTRIBUTE_ASSIGNMENT_KEY = TextAttributesKey.createTextAttributesKey(
		"bnd.attributeAssignment", ManifestColorsAndFonts.HEADER_ASSIGNMENT_KEY);

	public static final TextAttributesKey ATTRIBUTE_NAME_KEY = TextAttributesKey.createTextAttributesKey(
		"bnd.attributeName", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

	public static final TextAttributesKey ATTRIBUTE_VALUE_KEY = TextAttributesKey.createTextAttributesKey(
		"bnd.attributeValue", DefaultLanguageHighlighterColors.STRING);

	public static final TextAttributesKey CLAUSE_SEPARATOR_KEY = TextAttributesKey.createTextAttributesKey(
		"bnd.clauseSeparator", DefaultLanguageHighlighterColors.COMMA);

	public static final TextAttributesKey DIRECTIVE_ASSIGNMENT_KEY = TextAttributesKey.createTextAttributesKey(
		"bnd.directiveAssignment", ManifestColorsAndFonts.HEADER_ASSIGNMENT_KEY);

	public static final TextAttributesKey DIRECTIVE_NAME_KEY = TextAttributesKey.createTextAttributesKey(
		"bnd.directiveName", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

	public static final TextAttributesKey DIRECTIVE_VALUE_KEY = TextAttributesKey.createTextAttributesKey(
		"bnd.directiveValue", DefaultLanguageHighlighterColors.STRING);

	public static final TextAttributesKey PARAMETER_SEPARATOR_KEY = TextAttributesKey.createTextAttributesKey(
		"bnd.parameterSeparator", DefaultLanguageHighlighterColors.SEMICOLON);

	private OsgiManifestColorsAndFonts() {
	}

}