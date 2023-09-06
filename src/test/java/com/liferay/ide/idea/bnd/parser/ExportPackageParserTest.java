/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.parser;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.LightProjectDescriptor;
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
public class ExportPackageParserTest extends LightJavaCodeInsightFixtureTestCase {

	@Test
	public void testInvalidExportPackageHighlighting() {
		myFixture.configureByFiles("invalidExportPackage/bnd.bnd", "com/liferay/test/Foo.java");

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting(HighlightSeverity.WARNING);

		assertFalse(highlightInfos.isEmpty());

		HighlightInfo highlightInfo = highlightInfos.get(0);

		assertEquals(highlightInfo.getDescription(), "Cannot resolve package com.liferay.non.existing");
	}

	@Test
	public void testInvalidExportPackageUsesHighlighting() {
		myFixture.configureByFiles(
			"invalidExportPackageUses/bnd.bnd", "com/liferay/test/Foo.java", "com/liferay/foo/Bar.java");

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting(HighlightSeverity.WARNING);

		assertFalse(highlightInfos.isEmpty());

		HighlightInfo highlightInfo = highlightInfos.get(0);

		assertEquals(highlightInfo.getDescription(), "Cannot resolve package com.liferay.non.existing");
	}

	@Test
	public void testResolvePackage() {
		myFixture.configureByFiles("resolvePackage/bnd.bnd", "com/liferay/test/Foo.java");

		PsiFile psiFile = myFixture.getFile();

		PsiElement element = psiFile.findElementAt(myFixture.getCaretOffset());

		PsiElement parentElement = element.getParent();

		PsiReference[] references = parentElement.getReferences();

		assertNotNull(references);
		assertEquals(references.length, 3);

		PsiElement resolve1 = references[0].resolve();
		PsiElement resolve2 = references[1].resolve();
		PsiElement resolve3 = references[2].resolve();

		assertNotNull(resolve1);
		assertNotNull(resolve2);
		assertNotNull(resolve3);

		assertInstanceOf(resolve1, PsiPackage.class);
		assertInstanceOf(resolve2, PsiPackage.class);
		assertInstanceOf(resolve3, PsiPackage.class);

		PsiPackage package1 = (PsiPackage)resolve1;
		PsiPackage package2 = (PsiPackage)resolve2;
		PsiPackage package3 = (PsiPackage)resolve3;

		assertEquals("com", package1.getName());
		assertEquals("liferay", package2.getName());
		assertEquals("test", package3.getName());
	}

	@Test
	public void testResolvePackageUses() {
		myFixture.configureByFiles(
			"resolvePackageUses/bnd.bnd", "com/liferay/test/Foo.java", "com/liferay/foo/Bar.java");

		PsiFile psiFile = myFixture.getFile();

		PsiElement element = psiFile.findElementAt(myFixture.getCaretOffset());

		PsiElement parentElement = element.getParent();

		PsiReference[] references = parentElement.getReferences();

		assertNotNull(references);
		assertEquals(references.length, 3);

		PsiElement resolve1 = references[0].resolve();
		PsiElement resolve2 = references[1].resolve();
		PsiElement resolve3 = references[2].resolve();

		assertNotNull(resolve1);
		assertNotNull(resolve2);
		assertNotNull(resolve3);

		assertInstanceOf(resolve1, PsiPackage.class);
		assertInstanceOf(resolve2, PsiPackage.class);
		assertInstanceOf(resolve3, PsiPackage.class);

		PsiPackage package1 = (PsiPackage)resolve1;
		PsiPackage package2 = (PsiPackage)resolve2;
		PsiPackage package3 = (PsiPackage)resolve3;

		assertEquals("com", package1.getName());
		assertEquals("liferay", package2.getName());
		assertEquals("foo", package3.getName());
	}

	@Test
	public void testValidExportPackageHighlighting() {
		myFixture.configureByFiles("validExportPackage/bnd.bnd", "com/liferay/test/Foo.java");

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting(HighlightSeverity.WARNING);

		assertTrue(
			"Valid Export Package should not highlight any errors, but test found the following hightlights: " +
				highlightInfos,
			highlightInfos.isEmpty());
	}

	@Test
	public void testValidExportPackageUsesHighlighting() {
		myFixture.configureByFiles(
			"validExportPackageUses/bnd.bnd", "com/liferay/test/Foo.java", "com/liferay/foo/Bar.java");

		List<HighlightInfo> highlightInfos = myFixture.doHighlighting(HighlightSeverity.WARNING);

		assertTrue(
			"Valid Export Package with Uses-Statement should not highlight any errors," +
				"but test found the following hightlights: " + highlightInfos,
			highlightInfos.isEmpty());
	}

	@NotNull
	@Override
	protected LightProjectDescriptor getProjectDescriptor() {
		return _JAVA_DESCRIPTOR;
	}

	@Override
	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	private static final LightProjectDescriptor _JAVA_DESCRIPTOR = new DefaultLightProjectDescriptor() {

		@Override
		public void configureModule(
			@NotNull Module module, @NotNull ModifiableRootModel modifiableRootModel,
			@NotNull ContentEntry contentEntry) {

			LanguageLevelModuleExtension extension = modifiableRootModel.getModuleExtension(
				LanguageLevelModuleExtension.class);

			if (extension != null) {
				extension.setLanguageLevel(LanguageLevel.JDK_1_8);
			}

			modifiableRootModel.setSdk(SdkUtil.getTestJdk());

			File testDataDir = new File(_TEST_DATA_PATH);

			final String testDataPath = PathUtil.toSystemIndependentName(testDataDir.getAbsolutePath());

			VfsRootAccess.allowRootAccess(Disposer.newDisposable(), testDataPath);
		}

	};

	private static final String _TEST_DATA_PATH = "testdata/com/liferay/ide/idea/bnd/parser/ExportPackageParserTest";

}