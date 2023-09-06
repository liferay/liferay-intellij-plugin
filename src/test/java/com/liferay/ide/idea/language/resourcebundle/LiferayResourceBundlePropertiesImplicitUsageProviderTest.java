/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.resourcebundle;

import com.intellij.lang.properties.codeInspection.unused.UnusedPropertyInspection;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayResourceBundlePropertiesImplicitUsageProviderTest extends BasePlatformTestCase {

	@Test
	public void testImplicitUsageArbitraryPropertyInLanguageProperties() {
		myFixture.configureByFiles("Language_unused.properties");

		//Language.properties should show unused warning for other properties
		myFixture.checkHighlighting();
	}

	@Test
	public void testImplicitUsageJavaxPortletTitleInLanguageProperties() {
		myFixture.configureByFiles("Language.properties");

		//Language.properties should not show any unused warning,
		//even if javax.portlet.title.my_portlet is not used explicitly
		myFixture.checkHighlighting();
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/resourcebundle" +
			"/LiferayResourceBundlePropertiesImplicitUsageProviderTest";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		myFixture.enableInspections(new UnusedPropertyInspection());
	}

}