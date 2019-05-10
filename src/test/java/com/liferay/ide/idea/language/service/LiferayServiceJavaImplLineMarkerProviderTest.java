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

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 * @author Gregory Amerson
 */
public class LiferayServiceJavaImplLineMarkerProviderTest extends LightCodeInsightFixtureTestCase {

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void testExceptionNameInspection() {
		myFixture.configureByFiles("com/liferay/ide/model/impl/MyModelImpl.java", "service.xml");

		AtomicBoolean lineMarkerFound = new AtomicBoolean(false);

		List<GutterMark> allGutterMarks = myFixture.findAllGutters();

		Stream<GutterMark> s = allGutterMarks.stream();

		s.filter(
			gutterMark -> gutterMark instanceof LineMarkerInfo.LineMarkerGutterIconRenderer
		).map(
			gutterMark -> {
				LineMarkerInfo.LineMarkerGutterIconRenderer lineMarkerGutterIconRenderer =
					(LineMarkerInfo.LineMarkerGutterIconRenderer)gutterMark;

				return lineMarkerGutterIconRenderer.getLineMarkerInfo();
			}
		).filter(
			lineMarkerInfo -> lineMarkerInfo instanceof RelatedItemLineMarkerInfo
		).flatMap(
			lineMarkerInfo -> {
				RelatedItemLineMarkerInfo relatedItemLineMarkerInfo = (RelatedItemLineMarkerInfo)lineMarkerInfo;

				Collection<GotoRelatedItem> items = relatedItemLineMarkerInfo.createGotoRelatedItems();

				return items.stream();
			}
		).map(
			GotoRelatedItem::getElement
		).filter(
			Objects::nonNull
		).findAny(
		).ifPresent(
			e -> lineMarkerFound.set(true)
		);

		assertTrue("Java Implementation line marker not found", lineMarkerFound.get());
	}

	@NotNull
	@Override
	protected LightProjectDescriptor getProjectDescriptor() {
		return _MY_PROJECT_DESCRIPTOR;
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/service/LiferayServiceJavaImplLineMarkerProviderTest";
	}

	private static final LightProjectDescriptor _MY_PROJECT_DESCRIPTOR = new DefaultLightProjectDescriptor() {

		@Override
		public void configureModule(
			@NotNull Module module, @NotNull ModifiableRootModel modifiableRootModel,
			@NotNull ContentEntry contentEntry) {

			LanguageLevelModuleExtension languageLevelModuleExtension = modifiableRootModel.getModuleExtension(
				LanguageLevelModuleExtension.class);

			if (languageLevelModuleExtension != null) {
				languageLevelModuleExtension.setLanguageLevel(LanguageLevel.JDK_1_8);
			}

			JavaAwareProjectJdkTableImpl javaAwareProjectJdkTableImpl = JavaAwareProjectJdkTableImpl.getInstanceEx();

			Sdk sdk = javaAwareProjectJdkTableImpl.getInternalJdk();

			modifiableRootModel.setSdk(sdk);
		}

		@Override
		public Sdk getSdk() {
			return IdeaTestUtil.getMockJdk18();
		}

	};

}