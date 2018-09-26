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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.List;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibResourceBundleReferenceContributorTest extends LightCodeInsightFixtureTestCase {

	public void testCompletion() {
		myFixture.configureByFiles("view.jsp", "liferay-ui.tld", "Language.properties", "foo.properties");
		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue("\"lang\" should have been resolved in Language.properties", strings.contains("lang"));
		assertFalse("\"foo\" should not be resolved, because it is in a non-Language file", strings.contains("foo"));
	}

	public void testInvalidReference() {
		myFixture.configureByFiles("invalid.jsp", "liferay-ui.tld", "Language.properties", "foo.properties");

		PsiFile file = myFixture.getFile();

		PsiElement element = file.findElementAt(myFixture.getCaretOffset()).getParent();

		PsiElement resolve = element.getReferences()[0].resolve();

		assertTrue("\"foo\" should not be resolvable, because it is in a non-Language file", resolve == null);
	}

	public void testReference() {
		myFixture.configureByFiles("edit.jsp", "liferay-ui.tld", "Language.properties", "foo.properties");

		PsiFile file = myFixture.getFile();

		PsiElement element = file.findElementAt(myFixture.getCaretOffset()).getParent();

		PsiElement resolve = element.getReferences()[0].resolve();

		assertTrue("\"lang\" should be resolvable, because it is in Language.properties", resolve != null);
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/tag/LiferayTaglibResourceBundleReferenceContributorTest";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

}