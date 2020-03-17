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

package com.liferay.ide.idea.bnd.actions;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

/**
 * @author Dominik Marks
 */
public class EnterInBndFileHandlerTest extends LightPlatformCodeInsightFixtureTestCase {

	public void testEnterInBndFileHandler() {
		myFixture.configureByFiles("bnd.bnd");

		myFixture.type('\n');

		myFixture.checkResultByFile("updated.bnd");
	}

	@Override
	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	private static final String _TEST_DATA_PATH = "testdata/com/liferay/ide/idea/bnd/actions/EnterInBndFileHandlerTest";

}