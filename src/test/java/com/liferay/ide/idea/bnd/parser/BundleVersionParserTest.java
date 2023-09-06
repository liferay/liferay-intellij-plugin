/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.parser;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class BundleVersionParserTest extends BasePlatformTestCase {

	@Test
	public void testInvalidBundleVersionHighlighting() {
		myFixture.configureByText("bnd.bnd", "Bundle-Version: foo.bar\n");

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting();

		assertFalse(highlightInfos.isEmpty());

		HighlightInfo highlightInfo = highlightInfos.get(0);

		assertEquals(highlightInfo.getDescription(), "invalid version \"foo.bar\": non-numeric \"foo\"");
	}

	@Test
	public void testValidBundleVersionHighlighting() {
		myFixture.configureByText("bnd.bnd", "Bundle-Version: 1.0.0\n");

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting();

		assertTrue(highlightInfos.isEmpty());
	}

}