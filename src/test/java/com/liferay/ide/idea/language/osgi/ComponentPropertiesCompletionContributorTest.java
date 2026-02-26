/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.osgi;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.PathUtil;

import com.liferay.ide.idea.util.SdkUtil;

import java.io.File;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class ComponentPropertiesCompletionContributorTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
	public void testComponentPropertiesCompletion() {
		myFixture.configureByFile("MyComponent.java");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(strings.contains("osgi.command.scope"));
	}

	@Test
	public void testMultiServiceComponentPropertiesCompletion() {
		myFixture.configureByFiles(
			"MultiServiceComponent.java", "com/liferay/portal/kernel/portlet/bridges/mvc/MVCActionCommand.java",
			"com/liferay/portal/kernel/search/IndexerPostProcessor.java");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue("should contain properties from IndexerPostProcessor", strings.contains("indexer.class.name"));
		assertTrue("should contain properties from MVCActionCommand", strings.contains("mvc.command.name"));
	}

	@Test
	public void testSinglePropertyComponentPropertiesCompletion() {
		myFixture.configureByFile("SinglePropertyComponent.java");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(
			"Single property annotation should offer \"osgi.command.scope\" for java.lang.Object component",
			strings.contains("osgi.command.scope"));
	}

	@NotNull
	@Override
	protected LightProjectDescriptor getProjectDescriptor() {
		return _JAVA_OSGI_LIB_DESCRIPTOR;
	}

	@Override
	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	private static final LightProjectDescriptor _JAVA_OSGI_LIB_DESCRIPTOR = new DefaultLightProjectDescriptor() {

		@Override
		public void configureModule(
			@NotNull Module module, @NotNull ModifiableRootModel modifiableRootModel,
			@NotNull ContentEntry contentEntry) {

			LanguageLevelModuleExtension extension = modifiableRootModel.getModuleExtension(
				LanguageLevelModuleExtension.class);

			if (extension != null) {
				extension.setLanguageLevel(LanguageLevel.JDK_1_8);
			}

			Sdk testJdk = SdkUtil.getTestJdk();

			SdkUtil.maybeAddSdk(testJdk);

			modifiableRootModel.setSdk(testJdk);

			File testDataDir = new File(_TEST_DATA_PATH);

			final String testDataPath = PathUtil.toSystemIndependentName(testDataDir.getAbsolutePath());

			VfsRootAccess.allowRootAccess(Disposer.newDisposable(), testDataPath);

			PsiTestUtil.addLibrary(modifiableRootModel, "OSGi", testDataPath, "osgi.jar");
		}

	};

	private static final String _TEST_DATA_PATH =
		"testdata/com/liferay/ide/idea/language/osgi/ComponentPropertiesCompletionContributorTest";

}