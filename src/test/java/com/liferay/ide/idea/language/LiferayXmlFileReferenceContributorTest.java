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

package com.liferay.ide.idea.language;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.List;

/**
 * @author Dominik Marks
 */
public class LiferayXmlFileReferenceContributorTest extends LightCodeInsightFixtureTestCase {

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