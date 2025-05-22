/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.tag;

import com.intellij.codeInsight.completion.CompletionType;
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

import org.jetbrains.annotations.NotNull;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibSearchContainerJavaBeanReferenceContributorTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
	public void testJavaBeanReferenceLookup() {
		myFixture.configureByFiles("view.jsp", "liferay-ui.tld", "com/liferay/ide/model/MyModel.java");
		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(strings.contains("description"));
	}

	@NotNull
	@Override
	protected LightProjectDescriptor getProjectDescriptor() {
		return _lightProjectDescriptor;
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/tag" +
			"/LiferayTaglibSearchContainerJavaBeanReferenceContributorTest";
	}

	private static final LightProjectDescriptor _lightProjectDescriptor = new DefaultLightProjectDescriptor() {

		@Override
		public void configureModule(
			@NotNull Module module, @NotNull ModifiableRootModel modifiableRootModel,
			@NotNull ContentEntry contentEntry) {

			Sdk testJdk = SdkUtil.getTestJdk();

			SdkUtil.maybeAddSdk(testJdk);

			LanguageLevelModuleExtension languageLevelModuleExtension = modifiableRootModel.getModuleExtension(
				LanguageLevelModuleExtension.class);

			if (languageLevelModuleExtension != null) {
				languageLevelModuleExtension.setLanguageLevel(LanguageLevel.JDK_1_8);
			}

			modifiableRootModel.setSdk(testJdk);
		}

		@Override
		public Sdk getSdk() {
			return IdeaTestUtil.getMockJdk18();
		}

	};

}