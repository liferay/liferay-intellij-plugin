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

package com.liferay.ide.idea.bndtools.completion;

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
		@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
		@NotNull CompletionResultSet result) {

		for (String name : _names) {
			boolean directive = StringUtil.endsWithChar(name, ':');

			InsertHandler<LookupElement> insertHandler = _attributeHandler;

			if (directive) {
				name = name.substring(0, name.length() - 1);
				insertHandler = _directiveHandler;
			}

			result.addElement(
				LookupElementBuilder.create(
					name
				).withCaseSensitivity(
					false
				).withInsertHandler(
					insertHandler
				)
			);
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