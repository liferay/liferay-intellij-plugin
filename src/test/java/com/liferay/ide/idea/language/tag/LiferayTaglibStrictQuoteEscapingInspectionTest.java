/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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
public class LiferayTaglibStrictQuoteEscapingInspectionTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
	public void testStrictQuoteEscapingInspection() {
		myFixture.configureByFiles("view.jsp", "liferay-ui.tld");
		myFixture.checkHighlighting();

		List<IntentionAction> allQuickFixeIntentionActions = myFixture.getAllQuickFixes();

		for (IntentionAction quickFixIntentionAction : allQuickFixeIntentionActions) {
			if (Objects.equals(quickFixIntentionAction.getFamilyName(), "Use single quotes")) {
				myFixture.launchAction(quickFixIntentionAction);
			}
		}

		myFixture.checkResultByFile("view_fixed.jsp");
	}

	public void testValidStrictQuoteEscaping() {
		myFixture.configureByFiles("view_fixed.jsp", "liferay-ui.tld");
		myFixture.checkHighlighting();
	}

	@NotNull
	@Override
	protected LightProjectDescriptor getProjectDescriptor() {
		return _lightProjectDescriptor;
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/tag/LiferayTaglibStrictQuoteEscapingInspectionTest";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		myFixture.enableInspections(new LiferayTaglibStrictQuoteEscapingInspection());
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