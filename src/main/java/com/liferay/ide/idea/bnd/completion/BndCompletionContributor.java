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

package com.liferay.ide.idea.bnd.completion;

import static com.intellij.patterns.PlatformPatterns.psiElement;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;

import com.liferay.ide.idea.bnd.BndLanguage;
import com.liferay.ide.idea.bnd.parser.BndHeaderParsers;
import com.liferay.ide.idea.bnd.psi.BndHeader;
import com.liferay.ide.idea.bnd.psi.BndTokenType;
import com.liferay.ide.idea.bnd.psi.Directive;

import org.jetbrains.annotations.NotNull;

import org.osgi.framework.Constants;

/**
 * @author Charles Wu
 */
public class BndCompletionContributor extends CompletionContributor {

	public BndCompletionContributor() {
		extend(
			CompletionType.BASIC, _header(Constants.EXPORT_PACKAGE),
			new HeaderParametersProvider(Constants.VERSION_ATTRIBUTE, Constants.USES_DIRECTIVE + ':'));

		extend(
			CompletionType.BASIC, _header(Constants.IMPORT_PACKAGE),
			new HeaderParametersProvider(Constants.VERSION_ATTRIBUTE, Constants.RESOLUTION_DIRECTIVE + ':'));

		extend(
			CompletionType.BASIC, _directive(Constants.RESOLUTION_DIRECTIVE),
			new SimpleProvider(Constants.RESOLUTION_MANDATORY, Constants.RESOLUTION_OPTIONAL));

		extend(
			CompletionType.BASIC, _bndHeader(),
			new CompletionProvider<CompletionParameters>() {

				@Override
				public void addCompletions(
					@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
					@NotNull CompletionResultSet resultSet) {

					for (String header : BndHeaderParsers.parsersMap.keySet()) {
						resultSet.addElement(
							LookupElementBuilder.create(
								header
							).withInsertHandler(
								_headerInsertHandler
							));
					}
				}

			});
	}

	private ElementPattern<PsiElement> _bndHeader() {
		PsiElementPattern.Capture<PsiElement> element = psiElement(BndTokenType.HEADER_NAME);

		return element.withLanguage(BndLanguage.INSTANCE);
	}

	private ElementPattern<PsiElement> _directive(String name) {
		PsiElementPattern.Capture<PsiElement> element = psiElement(BndTokenType.HEADER_VALUE_PART);

		PsiElementPattern.Capture<Directive> directiveElement = psiElement(Directive.class);

		return element.withSuperParent(2, directiveElement.withName(name));
	}

	private ElementPattern<PsiElement> _header(String name) {
		PsiElementPattern.Capture<PsiElement> psiElementCapture = psiElement(BndTokenType.HEADER_VALUE_PART);

		psiElementCapture.afterLeaf(";");

		PsiElementPattern.Capture<BndHeader> headerElement = psiElement(BndHeader.class);

		return psiElementCapture.withSuperParent(3, headerElement.withName(name));
	}

	private static final InsertHandler<LookupElement> _headerInsertHandler = new InsertHandler<LookupElement>() {

		@Override
		public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
			context.setAddCompletionChar(false);

			EditorModificationUtil.insertStringAtCaret(context.getEditor(), ": ");
			context.commitDocument();
		}

	};

}