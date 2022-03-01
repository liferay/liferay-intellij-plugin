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

package com.liferay.ide.idea.language.service;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.util.List;
import java.util.Objects;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLDuplicateExceptionInspectionTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
	public void testDuplicateExceptionInspection() {
		myFixture.configureByFiles("service.xml");

		myFixture.checkHighlighting();

		List<IntentionAction> allQuickFixes = myFixture.getAllQuickFixes();

		for (IntentionAction quickFix : allQuickFixes) {
			if (Objects.equals("Remove entry", quickFix.getFamilyName())) {
				myFixture.launchAction(quickFix);

				break;
			}
		}

		myFixture.checkResultByFile("service_fixed.xml", true);
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/service/LiferayServiceXMLDuplicateExceptionInspectionTest";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		myFixture.enableInspections(new LiferayServiceXMLDuplicateExceptionInspection());
	}

}