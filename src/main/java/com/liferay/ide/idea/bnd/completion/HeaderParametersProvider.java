/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ProcessingContext;

import org.jetbrains.annotations.NotNull;

/**
 * @author Charles Wu
 */
public class HeaderParametersProvider extends CompletionProvider<CompletionParameters> {

	public HeaderParametersProvider(String... names) {
		_names = names;
	}

	@Override
	public void addCompletions(
		@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext,
		@NotNull CompletionResultSet completionResultSet) {

		for (String name : _names) {
			boolean directive = StringUtil.endsWithChar(name, ':');

			InsertHandler<LookupElement> insertHandler = _attributeHandler;

			if (directive) {
				name = name.substring(0, name.length() - 1);
				insertHandler = _directiveHandler;
			}

			completionResultSet.addElement(
				LookupElementBuilder.create(
					name
				).withCaseSensitivity(
					false
				).withInsertHandler(
					insertHandler
				));
		}
	}

	private static final InsertHandler<LookupElement> _attributeHandler = (context, lookupElement) -> {
		context.setAddCompletionChar(false);

		EditorModificationUtil.insertStringAtCaret(context.getEditor(), "=");

		context.commitDocument();
	};

	private static final InsertHandler<LookupElement> _directiveHandler = (context, lookupElement) -> {
		context.setAddCompletionChar(false);

		EditorModificationUtil.insertStringAtCaret(context.getEditor(), ":=");

		context.commitDocument();
	};

	private final String[] _names;

}