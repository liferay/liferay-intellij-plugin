/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.tag;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.util.List;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibJavascriptLanguageInjectorTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
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