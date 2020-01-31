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

package com.liferay.ide.idea.bnd.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.liferay.ide.idea.bnd.parser.BndHeaderParser;
import com.liferay.ide.idea.bnd.parser.BndHeaderParsers;
import com.liferay.ide.idea.bnd.psi.BndHeader;
import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;
import com.liferay.ide.idea.bnd.psi.BndToken;
import com.liferay.ide.idea.bnd.psi.BndTokenType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class BndHeaderValuePartImpl extends ASTWrapperPsiElement implements BndHeaderValuePart {

	public BndHeaderValuePartImpl(ASTNode node) {
		super(node);
	}

	@NotNull
	@Override
	public TextRange getHighlightingRange() {
		int endOffset = getTextRange().getEndOffset();
		PsiElement last = getLastChild();

		while (_isSpace(last)) {
			endOffset -= last.getTextLength();
			last = last.getPrevSibling();
		}

		int startOffset = getTextOffset();
		PsiElement first = getFirstChild();

		while ((startOffset < endOffset) && _isSpace(first)) {
			startOffset += first.getTextLength();
			first = first.getNextSibling();
		}

		return new TextRange(startOffset, endOffset);
	}

	@NotNull
	@Override
	public PsiReference[] getReferences() {
		if (getUnwrappedText().isEmpty()) {
			return PsiReference.EMPTY_ARRAY;
		}

		BndHeader bndHeader = PsiTreeUtil.getParentOfType(this, BndHeader.class);

		if (bndHeader != null) {
			BndHeaderParser bndHeaderParser = BndHeaderParsers.parsersMap.get(bndHeader.getName());
			if (bndHeaderParser != null) {
				return bndHeaderParser.getReferences(this);
			}
		}

		return PsiReference.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public String getUnwrappedText() {
		StringBuilder builder = new StringBuilder();

		for (PsiElement element = getFirstChild(); element != null; element = element.getNextSibling()) {
			if (!_isSpace(element)) {
				builder.append(element.getText());
			}
		}

		return builder.toString(
		).trim();
	}

	private static boolean _isSpace(PsiElement element) {
		if (element instanceof BndToken) {
			BndToken bndToken = (BndToken)element;

			if (_spaces.contains(bndToken.getTokenType())) {
				return true;
			}
		}

		return false;
	}

	private static final TokenSet _spaces = TokenSet.create(TokenType.WHITE_SPACE, BndTokenType.NEWLINE);

}