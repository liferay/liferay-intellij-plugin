/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.util.List;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayXmlSchemaProviderTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
	public void testCompletion() {
		myFixture.configureByFiles("portlet-model-hints.xml");
		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(strings.contains("max-length"));
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/LiferayXmlSchemaProviderTest";
	}

}