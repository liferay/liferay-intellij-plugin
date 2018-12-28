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

package com.liferay.ide.idea.bndtools.completion.header;

import static com.intellij.lang.PsiBuilderUtil.expect;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import com.liferay.ide.idea.bndtools.psi.OsgiManifestElementType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.header.HeaderParser;
import org.jetbrains.lang.manifest.header.impl.StandardHeaderParser;
import org.jetbrains.lang.manifest.parser.ManifestParser;
import org.jetbrains.lang.manifest.psi.ManifestElementType;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;

/**
 * @author Charles Wu
 */
public class OsgiHeaderParser extends StandardHeaderParser {

	public static final HeaderParser INSTANCE = new OsgiHeaderParser();

	@Override
	public void parse(@NotNull PsiBuilder builder) {
		while (!builder.eof()) {
			if (!_parseClause(builder)) {
				break;
			}

			IElementType tokenType = builder.getTokenType();

			if (ManifestParser.HEADER_END_TOKENS.contains(tokenType)) {
				break;
			}
			else if (tokenType == ManifestTokenType.COMMA) {
				builder.advanceLexer();
			}
		}
	}

	private static boolean _parseAttribute(PsiBuilder builder, PsiBuilder.Marker marker) {
		builder.advanceLexer();

		boolean result = _parsesubClause(builder, true);
		marker.done(OsgiManifestElementType.ATTRIBUTE);

		return result;
	}

	private static boolean _parseClause(PsiBuilder builder) {
		PsiBuilder.Marker clause = builder.mark();

		boolean result = true;

		while (!builder.eof()) {
			if (!_parsesubClause(builder, false)) {
				result = false;

				break;
			}

			IElementType tokenType = builder.getTokenType();

			if (_clauseEndTokens.contains(tokenType)) {
				break;
			}
			else if (tokenType == ManifestTokenType.SEMICOLON) {
				builder.advanceLexer();
			}
		}

		clause.done(OsgiManifestElementType.CLAUSE);

		return result;
	}

	private static boolean _parseDirective(PsiBuilder builder, PsiBuilder.Marker marker) {
		builder.advanceLexer();

		if (expect(builder, ManifestTokenType.NEWLINE)) {
			expect(builder, ManifestTokenType.SIGNIFICANT_SPACE);
		}

		expect(builder, ManifestTokenType.EQUALS);

		boolean result = _parsesubClause(builder, true);

		marker.done(OsgiManifestElementType.DIRECTIVE);

		return result;
	}

	private static void _parseQuotedString(PsiBuilder builder) {
		do {
			builder.advanceLexer();
		}
		while (!builder.eof() && !ManifestParser.HEADER_END_TOKENS.contains(builder.getTokenType()) &&
			   !expect(builder, ManifestTokenType.QUOTE));
	}

	private static boolean _parsesubClause(PsiBuilder builder, boolean assignment) {
		PsiBuilder.Marker marker = builder.mark();
		boolean result = true;

		while (!builder.eof()) {
			IElementType tokenType = builder.getTokenType();

			if (_subclauseEndTokens.contains(tokenType)) {
				break;
			}
			else if (tokenType == ManifestTokenType.QUOTE) {
				_parseQuotedString(builder);
			}
			else if (!assignment && (tokenType == ManifestTokenType.EQUALS)) {
				marker.done(ManifestElementType.HEADER_VALUE_PART);

				return _parseAttribute(builder, marker.precede());
			}
			else if (!assignment && (tokenType == ManifestTokenType.COLON)) {
				marker.done(ManifestElementType.HEADER_VALUE_PART);

				return _parseDirective(builder, marker.precede());
			}
			else {
				IElementType lastToken = builder.getTokenType();
				builder.advanceLexer();

				if ((lastToken == ManifestTokenType.NEWLINE) &&
					(builder.getTokenType() != ManifestTokenType.SIGNIFICANT_SPACE)) {

					result = false;

					break;
				}
			}
		}

		marker.done(ManifestElementType.HEADER_VALUE_PART);

		return result;
	}

	private static final TokenSet _clauseEndTokens = TokenSet.orSet(
		ManifestParser.HEADER_END_TOKENS, TokenSet.create(ManifestTokenType.COMMA));
	private static final TokenSet _subclauseEndTokens = TokenSet.orSet(
		_clauseEndTokens, TokenSet.create(ManifestTokenType.SEMICOLON));

}