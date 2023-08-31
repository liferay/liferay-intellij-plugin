/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.tag;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.util.List;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibResourceBundleReferenceContributorTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
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

		PsiElement element = file.findElementAt(myFixture.getCaretOffset());

		PsiElement parentElement = element.getParent();

		PsiReference[] references = parentElement.getReferences();

		PsiElement resolve = references[0].resolve();

		assertTrue("\"foo\" should not be resolvable, because it is in a non-Language file", resolve == null);
	}

	public void testReference() {
		myFixture.configureByFiles("edit.jsp", "liferay-ui.tld", "Language.properties", "foo.properties");

		PsiFile file = myFixture.getFile();

		PsiElement element = file.findElementAt(myFixture.getCaretOffset());

		PsiElement parentElement = element.getParent();

		PsiReference[] references = parentElement.getReferences();

		PsiElement resolve = references[0].resolve();

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