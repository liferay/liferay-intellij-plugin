/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILeafElementType;

import com.liferay.ide.idea.bnd.BndLanguage;
import com.liferay.ide.idea.bnd.psi.impl.BndTokenImpl;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class BndTokenType extends IElementType implements ILeafElementType {

	public static final BndTokenType BACKSLASH_TOKEN = new BndTokenType("BACKSLASH_TOKEN");

	public static final BndTokenType CLOSING_BRACKET_TOKEN = new BndTokenType("CLOSING_BRACKET_TOKEN");

	public static final BndTokenType CLOSING_PARENTHESIS_TOKEN = new BndTokenType("CLOSING_PARENTHESIS_TOKEN");

	public static final BndTokenType COLON = new BndTokenType("COLON_TOKEN");

	public static final BndTokenType COMMA = new BndTokenType("COMMA_TOKEN");

	public static final BndTokenType COMMENT = new BndTokenType("COMMENT_TOKEN");

	public static final BndTokenType EQUALS = new BndTokenType("EQUALS_TOKEN");

	public static final BndTokenType HEADER_NAME = new BndTokenType("HEADER_NAME_TOKEN");

	public static final BndTokenType HEADER_VALUE_PART = new BndTokenType("HEADER_VALUE_PART_TOKEN");

	public static final BndTokenType NEWLINE = new BndTokenType("NEWLINE_TOKEN");

	public static final BndTokenType OPENING_BRACKET_TOKEN = new BndTokenType("OPENING_BRACKET_TOKEN");

	public static final BndTokenType OPENING_PARENTHESIS_TOKEN = new BndTokenType("OPENING_PARENTHESIS_TOKEN");

	public static final BndTokenType QUOTE = new BndTokenType("QUOTE_TOKEN");

	public static final BndTokenType SECTION_END = new BndTokenType("SECTION_END_TOKEN");

	public static final BndTokenType SEMICOLON = new BndTokenType("SEMICOLON_TOKEN");

	@NotNull
	@Override
	public ASTNode createLeafNode(CharSequence text) {
		return new BndTokenImpl(this, text);
	}

	private BndTokenType(@NonNls @NotNull String debugName) {
		super(debugName, BndLanguage.INSTANCE);
	}

}