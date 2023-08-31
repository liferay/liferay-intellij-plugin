/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.tag;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;

import org.jetbrains.annotations.NotNull;

/**
 * @author Terry Jia
 */
public class BooleanCompletionProvider extends CompletionProvider<CompletionParameters> {

	@Override
	protected void addCompletions(
		@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {

		result.addElement(LiferayLookupElementBuilderFactory.create("true", "boolean"));
		result.addElement(LiferayLookupElementBuilderFactory.create("false", "boolean"));

		result.stopHere();
	}

}