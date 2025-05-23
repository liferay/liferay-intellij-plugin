/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.jsp;

import com.intellij.debugger.engine.SourcesFinder;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.externalSystem.model.project.LibraryPathType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.PathUtil;

import com.liferay.ide.idea.util.SdkUtil;

import java.io.File;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayJspDebuggerSourceFinderAdapterTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
	public void testSourceFinderInLibrary() {
		SourcesFinder<JavaeeFacet[]> sourcesFinder = new LiferayJspDebuggerSourceFinderAdapter();

		PsiFile sourceFile = sourcesFinder.findSourceFile("init.jsp", myFixture.getProject(), new JavaeeFacet[0]);

		assertNotNull("SourcesFinder should have found \"init.jsp\" inside \"com.liferay.login.web.jar\"", sourceFile);
	}

	@Test
	public void testSourceFinderInTargetPlatformArtifacts() {
		SourcesFinder<JavaeeFacet[]> sourcesFinder = new LiferayJspDebuggerSourceFinderAdapter();

		PsiFile sourceFile = sourcesFinder.findSourceFile(
			"edit_article.jsp", myFixture.getProject(), new JavaeeFacet[0]);

		assertNotNull(
			"SourcesFinder should have found \"edit_article.jsp\" inside target platform artifacts", sourceFile);
	}

	@NotNull
	protected LightProjectDescriptor getProjectDescriptor() {
		return _JAVA_LOGIN_WEB_DESCRIPTOR;
	}

	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	@Override
	protected void setUp() throws Exception {
		File testFile = new File(_TEST_DATA_PATH);

		final String testDataPath = PathUtil.toSystemIndependentName(testFile.getAbsolutePath());

		final LibraryData libraryData = new LibraryData(GradleConstants.SYSTEM_ID, "Liferay Journal Web (Mock)", false);

		libraryData.setGroup("com.liferay");
		libraryData.setArtifactId("com.liferay.journal.web");
		libraryData.setVersion("1.0.0");

		File jarFile = new File(testDataPath, "com.liferay.journal.web.jar");

		assertTrue(jarFile.exists());

		libraryData.addPath(LibraryPathType.BINARY, jarFile.getPath());
		libraryData.addPath(LibraryPathType.SOURCE, jarFile.getPath());

		List<LibraryData> targetPlatformArtifacts = new ArrayList<>();

		targetPlatformArtifacts.add(libraryData);

		Field field = LiferayJspDebuggerSourceFinderAdapter.class.getDeclaredField("_targetPlatformArtifacts");

		field.setAccessible(true);
		field.set(null, targetPlatformArtifacts);

		super.setUp();
	}

	private static final LightProjectDescriptor _JAVA_LOGIN_WEB_DESCRIPTOR = new DefaultLightProjectDescriptor() {

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

			PsiTestUtil.addLibrary(
				modifiableRootModel, "com.liferay:com.liferay.login.web", testDataPath, "com.liferay.login.web.jar");
		}

	};

	private static final String _TEST_DATA_PATH =
		"testdata/com/liferay/ide/idea/language/jsp/LiferayJspDebuggerSourceFinderAdapterTest";

}