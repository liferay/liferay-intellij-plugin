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