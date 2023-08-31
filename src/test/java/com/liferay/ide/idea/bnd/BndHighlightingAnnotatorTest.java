/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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