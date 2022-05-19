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

package com.liferay.ide.idea.bnd;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

/**
 * @author Dominik Marks
 */
public class BndHighlightingAnnotatorTest extends BasePlatformTestCase {

	public void testLineCommentHighlighting() {
		myFixture.configureByFiles("bnd.bnd");

		EditorColorsManager editorColorsManager = EditorColorsManager.getInstance();

		EditorColorsScheme globalScheme = editorColorsManager.getGlobalScheme();

		TextAttributes lineCommentTextAttributes = globalScheme.getAttributes(
			OsgiManifestColorsAndFonts.LINE_COMMENT_KEY);

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting();

		for (HighlightInfo highlightInfo : highlightInfos) {
			if (highlightInfo.getSeverity() == HighlightSeverity.INFORMATION) {
				assertEquals(lineCommentTextAttributes, highlightInfo.getTextAttributes(null, globalScheme));
			}
			else {
				fail("unexpected Highlighting Info found: " + highlightInfo);
			}
		}
	}

	@Override
	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	private static final String _TEST_DATA_PATH = "testdata/com/liferay/ide/idea/bnd/BndHighlightingAnnotatorTest";

}