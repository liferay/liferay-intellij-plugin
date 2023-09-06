/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.resourcebundle;

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

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class ResourceBundleReferenceProviderTest extends LightJavaCodeInsightFixtureTestCase {

	public void testResourceBundleCodeCompletion() {
		myFixture.configureByFiles(
			"MyResourceBundleLoader.java", "java/util/ResourceBundle.java", "content/Language.properties",
			"content/Language_en.properties");
		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(strings.contains("content.Language"));
		assertTrue(strings.contains("content.Language_en"));
	}

	@NotNull
	@Override
	protected LightProjectDescriptor getProjectDescriptor() {
		return _lightProjectDescriptor;
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/resourcebundle/ResourceBundleReferenceProviderTest";
	}

	private static final LightProjectDescriptor _lightProjectDescriptor = new DefaultLightProjectDescriptor() {

		@Override
		public void configureModule(
			@NotNull Module module, @NotNull ModifiableRootModel modifiableRootModel,
			@NotNull ContentEntry contentEntry) {

			LanguageLevelModuleExtension languageLevelModuleExtension = modifiableRootModel.getModuleExtension(
				LanguageLevelModuleExtension.class);

			if (languageLevelModuleExtension != null) {
				languageLevelModuleExtension.setLanguageLevel(LanguageLevel.JDK_11);
			}
		}

		@Override
		public Sdk getSdk() {
			return IdeaTestUtil.getMockJdk(LanguageLevel.JDK_11.toJavaVersion());
		}

	};

}