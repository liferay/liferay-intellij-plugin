/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.tag;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * @author Terry Jia
 */
public class StringCompletionProvider extends CompletionProvider<CompletionParameters> {

	public StringCompletionProvider(String[] strings) {
		_strings = strings;
	}

	@Override
	protected void addCompletions(
		@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {

		Stream.of(
			_strings
		).map(
			s -> LiferayLookupElementBuilderFactory.create(s, "String")
		).forEach(
			result::addElement
		);

		result.stopHere();
	}

	private final String[] _strings;

}