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
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.List;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLExceptionNameInspectionTest extends LightCodeInsightFixtureTestCase {

    public void testExceptionNameInspection() {
        myFixture.configureByFiles("service.xml");

        myFixture.checkHighlighting();

        List<IntentionAction> allQuickFixes = myFixture.getAllQuickFixes();

        for (IntentionAction quickFix : allQuickFixes) {
            if ("Remove Exception suffix".equals(quickFix.getFamilyName())) {
                myFixture.launchAction(quickFix);
            }
        }

        myFixture.checkResultByFile("service_fixed.xml");
    }

    @Override
    protected String getTestDataPath() {
        return "testdata/com/liferay/ide/idea/language/service/LiferayServiceXMLExceptionNameInspectionTest";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        myFixture.enableInspections(new LiferayServiceXMLExceptionNameInspection());
    }

}