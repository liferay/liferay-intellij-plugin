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

package com.liferay.ide.idea.language;

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
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLLineMarkerProviderTest extends LightCodeInsightFixtureTestCase {

	@SuppressWarnings("unchecked")
	public void testExceptionNameInspection() {
		myFixture.configureByFiles("service.xml", "com/liferay/ide/model/impl/MyModelImpl.java");

		boolean lineMarkerFound = false;
		List<GutterMark> allMarkers = myFixture.findAllGutters();

		for (GutterMark gutterMark : allMarkers) {
			if (gutterMark instanceof LineMarkerInfo.LineMarkerGutterIconRenderer) {
				LineMarkerInfo.LineMarkerGutterIconRenderer lineMarkerGutterIconRenderer =
					(LineMarkerInfo.LineMarkerGutterIconRenderer)gutterMark;

				LineMarkerInfo lineMarkerInfo = lineMarkerGutterIconRenderer.getLineMarkerInfo();

				if (lineMarkerInfo instanceof RelatedItemLineMarkerInfo) {
					RelatedItemLineMarkerInfo relatedItemLineMarkerInfo = (RelatedItemLineMarkerInfo)lineMarkerInfo;

					Collection<GotoRelatedItem> gotoRelatedItems = relatedItemLineMarkerInfo.createGotoRelatedItems();

					if (!gotoRelatedItems.isEmpty()) {
						GotoRelatedItem gotoRelatedItem = gotoRelatedItems.iterator().next();

						PsiElement element = gotoRelatedItem.getElement();

						if (element != null) {
							lineMarkerFound = true;
						}
					}
				}
			}
		}

		assertTrue("service.xml line marker not found", lineMarkerFound);
	}

	@NotNull
	@Override
	protected LightProjectDescriptor getProjectDescriptor() {
		return _MY_PROJECT_DESCRIPTOR;
	}

	@Override
	protected String getTestDataPath() {
		return "testdata/com/liferay/ide/idea/language/LiferayServiceXMLLineMarkerProviderTest";
	}

	private static final LightProjectDescriptor _MY_PROJECT_DESCRIPTOR = new DefaultLightProjectDescriptor() {

		@Override
		public void configureModule(
			@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {

			LanguageLevelModuleExtension extension = model.getModuleExtension(LanguageLevelModuleExtension.class);

			if (extension != null) {
				extension.setLanguageLevel(LanguageLevel.JDK_1_8);
			}

			Sdk jdk = JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk();

			model.setSdk(jdk);
		}

		@Override
		public Sdk getSdk() {
			return IdeaTestUtil.getMockJdk18();
		}

	};

}