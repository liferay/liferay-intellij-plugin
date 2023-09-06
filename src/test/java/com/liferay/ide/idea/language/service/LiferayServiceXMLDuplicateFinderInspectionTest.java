/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.service;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.util.List;
import java.util.Objects;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLDuplicateFinderInspectionTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
	public void testDuplicateFinderInspection() {
		myFixture.configureByFiles("service.xml");

		myFixture.checkHighlighting();

		List<IntentionAction> allQuickFixes = myFixture.getAllQuickFixes();

		for (IntentionAction quickFix : allQuickFixes) {
			if (Objects.equals(quickFix.getFamilyName(), "Remove entry")) {
				myFixture.launchAction(quickFix);

				break;
			}
		}

		myFixture.checkResultByFile("service_fixed.xml", true);
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/service/LiferayServiceXMLDuplicateFinderInspectionTest";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		myFixture.enableInspections(new LiferayServiceXMLDuplicateFinderInspection());
	}

}