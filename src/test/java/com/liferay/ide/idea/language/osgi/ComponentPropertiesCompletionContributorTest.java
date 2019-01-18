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

package com.liferay.ide.idea.language.osgi;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.intellij.util.PathUtil;

import java.io.File;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class ComponentPropertiesCompletionContributorTest extends LightCodeInsightFixtureTestCase {

	public void testComponentPropertiesCompletion() {
		myFixture.configureByFile("MyComponent.java");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue(strings.contains("osgi.command.scope"));
	}

	public void testMultiServiceComponentPropertiesCompletion() {
		myFixture.configureByFiles(
			"MultiServiceComponent.java", "com/liferay/portal/kernel/portlet/bridges/mvc/MVCActionCommand.java",
			"com/liferay/portal/kernel/search/IndexerPostProcessor.java");

		myFixture.complete(CompletionType.BASIC, 1);

		List<String> strings = myFixture.getLookupElementStrings();

		assertTrue("should contain properties from IndexerPostProcessor", strings.contains("indexer.class.name"));
		assertTrue("should contain properties from MVCActionCommand", strings.contains("mvc.command.name"));
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
			@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {

			LanguageLevelModuleExtension extension = model.getModuleExtension(LanguageLevelModuleExtension.class);

			if (extension != null) {
				extension.setLanguageLevel(LanguageLevel.JDK_1_8);
			}

			Sdk internalJdk = JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk();

			model.setSdk(internalJdk);

			final String testDataPath = PathUtil.toSystemIndependentName(new File(_TEST_DATA_PATH).getAbsolutePath());

			VfsRootAccess.allowRootAccess(testDataPath);

			PsiTestUtil.addLibrary(module, model, "OSGi", testDataPath, "osgi.jar");
		}

	};

	private static final String _TEST_DATA_PATH =
		"testdata/com/liferay/ide/idea/language/osgi/ComponentPropertiesCompletionContributorTest";

}