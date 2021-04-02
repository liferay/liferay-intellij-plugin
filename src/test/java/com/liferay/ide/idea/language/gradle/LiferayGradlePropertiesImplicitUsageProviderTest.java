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

package com.liferay.ide.idea.language.gradle;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * @author Dominik Marks
 */
public class LiferayGradlePropertiesImplicitUsageProviderTest extends BasePlatformTestCase {

	public void testImplicitUsageJavaxPortletTitleInLanguageProperties() {
		myFixture.configureByFiles("gradle.properties");

		//gradle.properties should not show any unused warning,
		//even if liferay.workspace.modules.dir is not used explicitly
		myFixture.checkHighlighting();
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/gradle/LiferayGradlePropertiesImplicitUsageProviderTest";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

}