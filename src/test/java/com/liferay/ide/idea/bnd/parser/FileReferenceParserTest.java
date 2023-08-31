/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.parser;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class FileReferenceParserTest extends BasePlatformTestCase {

	@Test
	public void testFileReferenceContributor() {
		myFixture.configureByFiles("fileReferenceContributor/bnd.bnd", "configs/main.js");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(strings.contains("configs"));
	}

	@Test
	public void testInvalidFileReferenceHighlighting() {
		myFixture.configureByFiles("invalidFileReference/bnd.bnd", "configs/main.js");

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting();

		assertFalse(highlightInfos.isEmpty());

		HighlightInfo highlightInfo = highlightInfos.get(0);

		assertEquals(highlightInfo.getDescription(), "Cannot resolve file '/non/existing.js'");
	}

	@Test
	public void testResolveFile() {
		myFixture.configureByFiles("resolveFile/bnd.bnd", "configs/main.js");

		PsiFile psiFile = myFixture.getFile();

		PsiElement element = psiFile.findElementAt(myFixture.getCaretOffset());

		PsiElement parentElement = element.getParent();

		PsiReference[] references = parentElement.getReferences();

		assertNotNull(references);
		assertEquals(references.length, 2);

		PsiElement resolve = references[1].resolve();

		assertNotNull(resolve);

		assertInstanceOf(resolve, PsiFile.class);

		PsiFile targetPsiFile = (PsiFile)resolve;

		assertEquals("main.js", targetPsiFile.getName());
	}

	@Test
	public void testValidFileReferenceHighlighting() {
		myFixture.configureByFiles("validFileReference/bnd.bnd", "configs/main.js");

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting(HighlightSeverity.WARNING);

		assertTrue(highlightInfos.isEmpty());
	}

	@Override
	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	private static final String _TEST_DATA_PATH = "testdata/com/liferay/ide/idea/bnd/parser/FileReferenceParserTest";

}