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
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;

import org.jetbrains.annotations.NotNull;

/**
 * @author Charles Wu
 */
public class SimpleProvider extends CompletionProvider<CompletionParameters> {

	public SimpleProvider(String... items) {
		_items = items;
	}

	@Override
	public void addCompletions(
		@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
		@NotNull CompletionResultSet result) {

		for (String item : _items) {
			result.addElement(
				LookupElementBuilder.create(
					item
				).withCaseSensitivity(
					false
				)
			);
		}
	}

	private final String[] _items;

}