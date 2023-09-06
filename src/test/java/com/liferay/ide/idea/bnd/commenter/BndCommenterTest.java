/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.commenter;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * @author Dominik Marks
 */
public class BndCommenterTest extends BasePlatformTestCase {

	public void testEnterInBndFileHandler() {
		myFixture.configureByFiles("bnd.bnd");

		myFixture.performEditorAction("CommentByLineComment");

		myFixture.checkResultByFile("expected.bnd");
	}

	@Override
	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	private static final String _TEST_DATA_PATH = "testdata/com/liferay/ide/idea/bnd/commenter/BndCommenterTest";

}