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
public class LiferayXmlFileReferenceContributorTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
	public void testXmlAttributeFileReference() {
		myFixture.configureByFiles("default.xml", "my_resources.xml");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(strings.contains("my_resources.xml"));
	}

	public void testXmlAttributeRelativePathFileReference() {
		myFixture.configureByFiles("META-INF/resource-actions/default.xml", "META-INF/resource-actions/custom.xml");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(strings.contains("custom.xml"));
	}

	public void testXmlTagFileReference() {
		myFixture.configureByFiles("liferay-hook.xml", "custom_jsps/foo.txt");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(strings.contains("custom_jsps"));
	}

	@Override
	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	private static final String _TEST_DATA_PATH =
		"testdata/com/liferay/ide/idea/language/LiferayXmlFileReferenceContributorTest";

}