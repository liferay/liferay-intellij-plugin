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

package com.liferay.ide.idea.language.tag;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.List;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibJavascriptLanguageInjectorTest extends LightCodeInsightFixtureTestCase {

	public void testAuiValidatorCustomBody() {
		myFixture.configureByFiles("aui-validator-custom.jsp", "liferay-aui.tld");
		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(
			"Javascript code completion should be possible inside a <aui:validator name=\"custom\"> tag",
			strings.contains("alert"));
	}

	public void testAuiValidatorRequiredBody() {
		myFixture.configureByFiles("aui-validator-required.jsp", "liferay-aui.tld");
		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(
			"Javascript code completion should be possible inside a <aui:validator name=\"required\"> tag",
			strings.contains("alert"));
	}

	public void testJavascriptAttribute() {
		myFixture.configureByFiles("view.jsp", "liferay-aui.tld");
		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(
			"Javascript code completion should be possible inside the onClick attribute of a <aui:a> tag",
			strings.contains("alert"));
	}

	public void testJavascriptBody() {
		myFixture.configureByFiles("custom.jsp", "liferay-aui.tld");
		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(
			"Javascript code completion should be possible inside a <aui:script> tag", strings.contains("alert"));
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/tag/LiferayTaglibJavascriptLanguageInjectorTest";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

}