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
import com.liferay.ide.idea.bnd.psi.impl.BndFileImpl;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class BndParserDefinition implements ParserDefinition {

	public static final IFileElementType BND_FILE_ELEMENT_TYPE = new IFileElementType("BndFile", BndLanguage.INSTANCE);

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
		return TokenSet.EMPTY;
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