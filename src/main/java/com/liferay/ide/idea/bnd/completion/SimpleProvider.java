/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.completion;

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
		@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext,
		@NotNull CompletionResultSet completeResultSet) {

		for (String item : _items) {
			completeResultSet.addElement(
				LookupElementBuilder.create(
					item
				).withCaseSensitivity(
					false
				));
		}
	}

	private final String[] _items;

}