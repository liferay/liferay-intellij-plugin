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

package com.liferay.ide.idea.bnd.formatting;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class BndFormatterBlock extends AbstractBlock {

	public BndFormatterBlock(
		ASTNode astNode, Alignment alignment, Indent indent, Wrap wrap, CodeStyleSettings codeStyleSettings,
		SpacingBuilder spacingBuilder) {

		super(astNode, wrap, alignment);

		_indent = indent;

		_codeStyleSettings = codeStyleSettings;
		_spacingBuilder = spacingBuilder;
	}

	@Override
	public Indent getIndent() {
		return _indent;
	}

	@Nullable
	@Override
	public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
		return _spacingBuilder.getSpacing(this, child1, child2);
	}

	@Override
	public boolean isLeaf() {
		if (myNode.getFirstChildNode() == null) {
			return true;
		}

		return false;
	}

	@Override
	protected List<Block> buildChildren() {
		List<Block> blocks = new ArrayList<>();

		ASTNode childNode = myNode.getFirstChildNode();

		while (childNode != null) {
			if (childNode.getElementType() != TokenType.WHITE_SPACE) {
				Block block = new BndFormatterBlock(
					childNode, Alignment.createAlignment(), _indent, Wrap.createWrap(WrapType.NONE, false),
					_codeStyleSettings, _spacingBuilder);

				blocks.add(block);
			}

			childNode = childNode.getTreeNext();
		}

		return blocks;
	}

	private CodeStyleSettings _codeStyleSettings;
	private Indent _indent;
	private SpacingBuilder _spacingBuilder;

}