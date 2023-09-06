/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;

import com.liferay.ide.idea.bnd.BndLanguage;
import com.liferay.ide.idea.bnd.lexer.BndLexer;
import com.liferay.ide.idea.bnd.psi.BndElementType;
import com.liferay.ide.idea.bnd.psi.BndTokenType;
import com.liferay.ide.idea.bnd.psi.impl.BndFileImpl;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class BndParserDefinition implements ParserDefinition {

	public static final IFileElementType BND_FILE_ELEMENT_TYPE = new IFileElementType("BndFile", BndLanguage.INSTANCE);

	public static final TokenSet COMMENTS = TokenSet.create(BndTokenType.COMMENT);

	@NotNull
	@Override
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();

		if (elementType instanceof BndElementType) {
			BndElementType bndElementType = (BndElementType)elementType;

			return bndElementType.createPsi(node);
		}

		return PsiUtilCore.NULL_PSI_ELEMENT;
	}

	@Override
	public PsiFile createFile(FileViewProvider viewProvider) {
		return new BndFileImpl(viewProvider);
	}

	@NotNull
	@Override
	public Lexer createLexer(Project project) {
		return new BndLexer();
	}

	@Override
	public PsiParser createParser(Project project) {
		return new BndParser();
	}

	@NotNull
	@Override
	public TokenSet getCommentTokens() {
		return COMMENTS;
	}

	@Override
	public IFileElementType getFileNodeType() {
		return BND_FILE_ELEMENT_TYPE;
	}

	@NotNull
	@Override
	public TokenSet getStringLiteralElements() {
		return TokenSet.EMPTY;
	}

}