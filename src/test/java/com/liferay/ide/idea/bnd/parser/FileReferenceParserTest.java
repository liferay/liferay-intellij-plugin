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

package com.liferay.ide.idea.bnd.parser;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.util.List;

/**
 * @author Dominik Marks
 */
public class FileReferenceParserTest extends LightPlatformCodeInsightFixtureTestCase {

	public void testFileReferenceContributor() {
		myFixture.configureByFiles("fileReferenceContributor/bnd.bnd", "configs/main.js");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(strings.contains("configs"));
	}

	public void testInvalidFileReferenceHighlighting() {
		myFixture.configureByFiles("invalidFileReference/bnd.bnd", "configs/main.js");

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting();

		assertFalse(highlightInfos.isEmpty());

		HighlightInfo highlightInfo = highlightInfos.get(0);

		assertEquals(highlightInfo.getDescription(), "Cannot resolve file '/non/existing.js'");
	}

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