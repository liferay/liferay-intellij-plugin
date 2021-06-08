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

import static com.intellij.lang.PsiBuilderUtil.expect;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiReference;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import com.liferay.ide.idea.bnd.psi.BndElementType;
import com.liferay.ide.idea.bnd.psi.BndHeader;
import com.liferay.ide.idea.bnd.psi.BndHeaderValue;
import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;
import com.liferay.ide.idea.bnd.psi.BndTokenType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class BndHeaderParser {

	public static final BndHeaderParser INSTANCE = new BndHeaderParser();

	public boolean annotate(@NotNull BndHeader bndHeader, @NotNull AnnotationHolder annotationHolder) {
		return false;
	}

	@Nullable
	public Object getConvertedValue(@NotNull BndHeader bndHeader) {
		BndHeaderValue value = bndHeader.getBndHeaderValue();

		if (value != null) {
			return value.getUnwrappedText();
		}

		return null;
	}

	@NotNull
	public PsiReference[] getReferences(@NotNull BndHeaderValuePart bndHeaderValuePart) {
		return PsiReference.EMPTY_ARRAY;
	}

	public void parse(@NotNull PsiBuilder psiBuilder) {
		while (!psiBuilder.eof()) {
			if (!_parseClause(psiBuilder)) {
				break;
			}

			IElementType tokenType = psiBuilder.getTokenType();

			if (BndParser.HEADER_END_TOKENS.contains(tokenType)) {
				break;
			}
			else if (tokenType == BndTokenType.COMMA) {
				psiBuilder.advanceLexer();
			}
		}
	}

	private static boolean _parseAttribute(PsiBuilder psiBuilder, PsiBuilder.Marker marker) {
		psiBuilder.advanceLexer();

		boolean result = _parseSubclause(psiBuilder, true);

		marker.done(BndElementType.ATTRIBUTE);

		return result;
	}

	private static boolean _parseDirective(PsiBuilder psiBuilder, PsiBuilder.Marker marker) {
		psiBuilder.advanceLexer();

		if (expect(psiBuilder, BndTokenType.NEWLINE)) {
			expect(psiBuilder, TokenType.WHITE_SPACE);
		}

		expect(psiBuilder, BndTokenType.EQUALS);

		boolean result = _parseSubclause(psiBuilder, true);

		marker.done(BndElementType.DIRECTIVE);

		return result;
	}

	private static void _parseQuotedString(PsiBuilder psiBuilder) {
		do {
			psiBuilder.advanceLexer();
		}
		while (!psiBuilder.eof() && !BndParser.HEADER_END_TOKENS.contains(psiBuilder.getTokenType()) &&
			   !expect(psiBuilder, BndTokenType.QUOTE));
	}

	private static boolean _parseSubclause(PsiBuilder psiBuilder, boolean assignment) {
		PsiBuilder.Marker marker = psiBuilder.mark();
		boolean result = true;

		while (!psiBuilder.eof()) {
			IElementType tokenType = psiBuilder.getTokenType();

			String tokenText = psiBuilder.getTokenText();

			if (tokenText != null) {
				tokenText = tokenText.trim();

				//do not parse single backslashes as clause

				if (tokenText.equals("\\")) {
					psiBuilder.advanceLexer();

					marker.drop();

					return result;
				}
			}

			if (_subclauseEndTokens.contains(tokenType)) {
				break;
			}
			else if (tokenType == BndTokenType.QUOTE) {
				_parseQuotedString(psiBuilder);
			}
			else if (!assignment && (tokenType == BndTokenType.EQUALS)) {
				marker.done(BndElementType.HEADER_VALUE_PART);

				return _parseAttribute(psiBuilder, marker.precede());
			}
			else if (!assignment && (tokenType == BndTokenType.COLON)) {
				marker.done(BndElementType.HEADER_VALUE_PART);

				return _parseDirective(psiBuilder, marker.precede());
			}
			else {
				IElementType lastToken = psiBuilder.getTokenType();

				psiBuilder.advanceLexer();

				if ((psiBuilder.getTokenType() == BndTokenType.NEWLINE) &&
					!(lastToken == BndTokenType.BACKSLASH_TOKEN)) {

					break;
				}
			}
		}

		marker.done(BndElementType.HEADER_VALUE_PART);

		return result;
	}

	private boolean _parseClause(PsiBuilder psiBuilder) {
		PsiBuilder.Marker clauseMarker = psiBuilder.mark();

		boolean result = true;

		while (!psiBuilder.eof()) {
			if (!_parseSubclause(psiBuilder, false)) {
				result = false;

				break;
			}

			IElementType tokenType = psiBuilder.getTokenType();

			if (_clauseEndTokens.contains(tokenType)) {
				break;
			}
			else if (tokenType == BndTokenType.SEMICOLON) {
				psiBuilder.advanceLexer();
			}
		}

		clauseMarker.done(BndElementType.CLAUSE);

		return result;
	}

	private static final TokenSet _clauseEndTokens = TokenSet.orSet(
		BndParser.HEADER_END_TOKENS, TokenSet.create(BndTokenType.COMMA));
	private static final TokenSet _subclauseEndTokens = TokenSet.orSet(
		_clauseEndTokens, TokenSet.create(BndTokenType.SEMICOLON));

}