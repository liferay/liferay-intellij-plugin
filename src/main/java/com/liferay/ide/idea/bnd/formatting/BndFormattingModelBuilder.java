/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.formatting;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.FormattingContext;
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
	public FormattingModel createModel(@NotNull FormattingContext formattingContext) {
		PsiElement element = formattingContext.getPsiElement();
		CodeStyleSettings settings = formattingContext.getCodeStyleSettings();

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