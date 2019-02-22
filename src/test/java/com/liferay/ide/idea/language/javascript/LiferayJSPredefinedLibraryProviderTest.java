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

package com.liferay.ide.idea.language.javascript;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.externalSystem.model.project.LibraryPathType;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.intellij.util.PathUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Dominik Marks
 */
public class LiferayJSPredefinedLibraryProviderTest extends LightCodeInsightFixtureTestCase {

	public void ignoreTestHtmlScriptTagLiferay() {
		//Does not work in <script>-Tags inside JSPs in IntelliJ 2018.3.x
		//See bug report here https://youtrack.jetbrains.com/issue/WEB-37355
		myFixture.configureByFiles("view.jsp");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> lookupElementStrings = myFixture.getLookupElementStrings();

		assertTrue(lookupElementStrings.contains("Liferay"));
	}

	public void setUp() throws Exception {
		final String testDataPath = PathUtil.toSystemIndependentName(new File(_TEST_DATA_PATH).getAbsolutePath());

		final LibraryData libraryData = new LibraryData(
			GradleConstants.SYSTEM_ID, "Liferay Frontend JS Web (Mock)", false);

		libraryData.setGroup("com.liferay");
		libraryData.setArtifactId("com.liferay.frontend.js.web");
		libraryData.setVersion("1.0.0");

		String jarPath = testDataPath + "/com.liferay.frontend.js.web.jar";

		libraryData.addPath(LibraryPathType.BINARY, jarPath);
		libraryData.addPath(LibraryPathType.SOURCE, jarPath);

		List<LibraryData> targetPlatformArtifacts = new ArrayList<>();

		targetPlatformArtifacts.add(libraryData);

		LiferayJSPredefinedLibraryProvider.setTargetPlatformArtifacts(targetPlatformArtifacts);

		super.setUp();
	}

	public void testAuiScriptTagLiferay() {
		myFixture.configureByFiles("aui.jsp", "liferay-aui.tld");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> lookupElementStrings = myFixture.getLookupElementStrings();

		assertTrue(lookupElementStrings.contains("Liferay"));
	}

	public void testJavascriptLiferay() {
		myFixture.configureByFiles("main.js");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> lookupElementStrings = myFixture.getLookupElementStrings();

		assertTrue(lookupElementStrings.contains("Liferay"));
	}

	@Override
	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	private static final String _TEST_DATA_PATH =
		"testdata/com/liferay/ide/idea/language/javascript/LiferayJSPredefinedLibraryProviderTest";

}