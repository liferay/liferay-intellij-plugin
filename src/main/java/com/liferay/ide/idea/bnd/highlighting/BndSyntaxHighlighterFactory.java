/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;

import com.liferay.ide.idea.bnd.lexer.BndLexer;
import com.liferay.ide.idea.bnd.psi.BndTokenType;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.highlighting.ManifestColorsAndFonts;

/**
 * @author Dominik Marks
 */
public class BndSyntaxHighlighterFactory extends SyntaxHighlighterFactory {

	public static final SyntaxHighlighter HIGHLIGHTER = new SyntaxHighlighterBase() {

		@NotNull
		@Override
		public Lexer getHighlightingLexer() {
			return new BndLexer();
		}

		@NotNull
		@Override
		public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
			return pack(_highlighterAttributes.get(tokenType));
		}

		private final Map<IElementType, TextAttributesKey> _highlighterAttributes =
			new HashMap<IElementType, TextAttributesKey>() {
				{
					put(BndTokenType.COLON, ManifestColorsAndFonts.HEADER_ASSIGNMENT_KEY);
					put(BndTokenType.HEADER_NAME, ManifestColorsAndFonts.HEADER_NAME_KEY);
					put(BndTokenType.HEADER_VALUE_PART, ManifestColorsAndFonts.HEADER_VALUE_KEY);
				}
			};

	};

	@NotNull
	@Override
	public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
		return HIGHLIGHTER;
	}

}