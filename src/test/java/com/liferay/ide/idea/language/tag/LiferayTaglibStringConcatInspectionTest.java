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

package com.liferay.ide.idea.language.tag;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import com.liferay.ide.idea.util.SdkUtil;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibStringConcatInspectionTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
	public void testStringConcatInspection() {
		myFixture.configureByFiles("view.jsp", "liferay-aui.tld");
		myFixture.checkHighlighting();

		List<IntentionAction> allQuickFixeIntentionActions = myFixture.getAllQuickFixes();

		for (IntentionAction quickFix : allQuickFixeIntentionActions) {
			if (Objects.equals(quickFix.getFamilyName(), "Wrap in JSP expression")) {
				myFixture.launchAction(quickFix);
			}
		}

		myFixture.checkResultByFile("view_fixed.jsp");
	}

	public void testValidStringConcat() {
		myFixture.configureByFiles("view_fixed.jsp", "liferay-aui.tld");
		myFixture.checkHighlighting();
	}

	@NotNull
	@Override
	protected LightProjectDescriptor getProjectDescriptor() {
		return _lightProjectDescriptor;
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/tag/LiferayTaglibStringConcatInspectionTest";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		myFixture.enableInspections(new LiferayTaglibStringConcatInspection());
	}

	private static final LightProjectDescriptor _lightProjectDescriptor = new DefaultLightProjectDescriptor() {

		@Override
		public void configureModule(
			@NotNull Module module, @NotNull ModifiableRootModel modifiableRootModel,
			@NotNull ContentEntry contentEntry) {

			LanguageLevelModuleExtension languageLevelModuleExtension = modifiableRootModel.getModuleExtension(
				LanguageLevelModuleExtension.class);

			if (languageLevelModuleExtension != null) {
				languageLevelModuleExtension.setLanguageLevel(LanguageLevel.JDK_1_8);
			}

			modifiableRootModel.setSdk(SdkUtil.getTestJdk());
		}

		@Override
		public Sdk getSdk() {
			return IdeaTestUtil.getMockJdk18();
		}

	};

}