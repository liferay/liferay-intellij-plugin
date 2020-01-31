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

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ObjectUtils;

import com.liferay.ide.idea.bnd.psi.BndElementType;
import com.liferay.ide.idea.bnd.psi.BndTokenType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.ManifestBundle;

/**
 * @author Dominik Marks
 */
public class BndParser implements PsiParser {

	public static final TokenSet HEADER_END_TOKENS = TokenSet.create(
		BndTokenType.SECTION_END, BndTokenType.HEADER_NAME);

	@NotNull
	@Override
	public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
		builder.setDebugMode(
			ApplicationManager.getApplication(
			).isUnitTestMode());

		PsiBuilder.Marker rootMarker = builder.mark();

		while (!builder.eof()) {
			_parseSection(builder);
		}

		rootMarker.done(root);

		return builder.getTreeBuilt();
	}

	private static void _consumeHeaderValue(PsiBuilder psiBuilder) {
		while (!psiBuilder.eof() && !HEADER_END_TOKENS.contains(psiBuilder.getTokenType())) {
			psiBuilder.advanceLexer();
		}
	}

	private void _parseHeader(PsiBuilder psiBuilder) {
		PsiBuilder.Marker headerMarker = psiBuilder.mark();
		String headerName = psiBuilder.getTokenText();

		psiBuilder.advanceLexer();

		if (psiBuilder.getTokenType() == BndTokenType.COLON) {
			psiBuilder.advanceLexer();

			BndHeaderParser bndHeaderParser = ObjectUtils.notNull(
				BndHeaderParsers.parsersMap.get(headerName), BndHeaderParser.INSTANCE);

			bndHeaderParser.parse(psiBuilder);
		}
		else {
			PsiBuilder.Marker marker = psiBuilder.mark();
			_consumeHeaderValue(psiBuilder);
			marker.error(ManifestBundle.message("manifest.colon.expected"));
		}

		headerMarker.done(BndElementType.HEADER);
	}

	private void _parseSection(PsiBuilder psiBuilder) {
		PsiBuilder.Marker sectionMarker = psiBuilder.mark();

		while (!psiBuilder.eof()) {
			IElementType tokenType = psiBuilder.getTokenType();

			if (tokenType == BndTokenType.HEADER_NAME) {
				_parseHeader(psiBuilder);
			}
			else if (tokenType == BndTokenType.SECTION_END) {
				psiBuilder.advanceLexer();

				break;
			}
			else {
				PsiBuilder.Marker marker = psiBuilder.mark();
				_consumeHeaderValue(psiBuilder);
				marker.error(ManifestBundle.message("manifest.header.expected"));
			}
		}

		sectionMarker.done(BndElementType.SECTION);
	}

}