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

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayServiceJavaImplLineMarkerProvider extends RelatedItemLineMarkerProvider {

	@Override
	@SuppressWarnings("rawtypes")
	protected void collectNavigationMarkers(
		@NotNull PsiElement psiElement, @NotNull Collection<? super RelatedItemLineMarkerInfo> lineMarkerInfos) {

		if ((psiElement instanceof PsiIdentifier) && (psiElement.getParent() instanceof PsiClass)) {
			PsiClass psiClass = (PsiClass)psiElement.getParent();

			String name = psiClass.getName();
			PsiJavaFile psiJavaFile = (PsiJavaFile)psiClass.getContainingFile();

			if ((psiJavaFile != null) && (name != null)) {
				String packageName = psiJavaFile.getPackageName();

				if (name.endsWith("Impl") && packageName.endsWith(".model.impl")) {
					String targetName = name.substring(0, name.length() - 4);
					String targetPackage = packageName.substring(0, packageName.length() - 11);

					Collection<PsiElement> targetPsiElements = new ArrayList<>();

					Project project = psiElement.getProject();

					PsiFile[] psiFiles = FilenameIndex.getFilesByName(
						project, "service.xml", GlobalSearchScope.allScope(project));

					for (PsiFile psiFile : psiFiles) {
						if (psiFile instanceof XmlFile) {
							XmlFile xmlFile = (XmlFile)psiFile;

							if (xmlFile.isValid()) {
								XmlTag rootXmlTag = xmlFile.getRootTag();

								if (rootXmlTag == null) {
									continue;
								}

								if (Objects.equals("service-builder", rootXmlTag.getLocalName())) {
									String packagePath = rootXmlTag.getAttributeValue("package-path");

									if (targetPackage.equals(packagePath)) {
										XmlTag[] xmlTags = rootXmlTag.findSubTags("entity");

										for (XmlTag xmlTag : xmlTags) {
											String entityName = xmlTag.getAttributeValue("name");

											if (targetName.equals(entityName)) {
												targetPsiElements.add(xmlTag);
											}
										}
									}
								}
							}
						}
					}

					if (!targetPsiElements.isEmpty()) {
						NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(
							AllIcons.Gutter.ImplementingMethod);

						builder.setTargets(targetPsiElements);
						builder.setTooltipText("Navigate to Declaration");

						lineMarkerInfos.add(builder.createLineMarkerInfo(psiElement));
					}
				}
			}
		}
	}

}