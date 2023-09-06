/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.gradle;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayGradlePropertiesImplicitUsageProviderTest extends BasePlatformTestCase {

	@Test
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