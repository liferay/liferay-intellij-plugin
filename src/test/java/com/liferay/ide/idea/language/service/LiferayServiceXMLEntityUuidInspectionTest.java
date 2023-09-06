/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.service;

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLEntityUuidInspectionTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
	public void testInvalidUuidInspection() {
		myFixture.configureByFiles("service_invalid.xml");

		myFixture.checkHighlighting();
	}

	@Test
	public void testValidUuidInspection() {
		myFixture.configureByFiles("service_valid.xml");

		myFixture.checkHighlighting();
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/service/LiferayServiceXMLEntityUuidInspectionTest";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		myFixture.enableInspections(new LiferayServiceXMLEntityUuidInspection());
	}

}