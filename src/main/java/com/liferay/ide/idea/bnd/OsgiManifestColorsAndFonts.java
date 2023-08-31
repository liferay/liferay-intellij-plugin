/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd;

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

	public static final TextAttributesKey LINE_COMMENT_KEY = TextAttributesKey.createTextAttributesKey(
		"bnd.lineComment", DefaultLanguageHighlighterColors.LINE_COMMENT);

	public static final TextAttributesKey PARAMETER_SEPARATOR_KEY = TextAttributesKey.createTextAttributesKey(
		"bnd.parameterSeparator", DefaultLanguageHighlighterColors.SEMICOLON);

	private OsgiManifestColorsAndFonts() {
	}

}