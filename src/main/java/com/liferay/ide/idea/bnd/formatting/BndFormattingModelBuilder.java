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
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.Indent;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;

import com.liferay.ide.idea.bnd.BndFileType;
import com.liferay.ide.idea.bnd.psi.BndElementType;
import com.liferay.ide.idea.bnd.psi.BndTokenType;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class BndFormattingModelBuilder implements FormattingModelBuilder {

	@NotNull
	@Override
	public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
		ASTNode astNode = element.getNode();

		PsiFile psiFile = element.getContainingFile();

		SpacingBuilder spacingBuilder = new SpacingBuilder(
			settings, psiFile.getLanguage()
		).between(
			BndTokenType.COLON, BndElementType.CLAUSE
		).spaces(
			1
		).between(
			BndTokenType.NEWLINE, BndTokenType.HEADER_VALUE_PART
		).spaces(
			settings.getIndentSize(BndFileType.INSTANCE)
		);

		BndFormatterBlock bndFormatterBlock = new BndFormatterBlock(
			astNode, Alignment.createAlignment(), Indent.getNoneIndent(), Wrap.createWrap(WrapType.NONE, false),
			settings, spacingBuilder);

		return FormattingModelProvider.createFormattingModelForPsiFile(psiFile, bndFormatterBlock, settings);
	}

}