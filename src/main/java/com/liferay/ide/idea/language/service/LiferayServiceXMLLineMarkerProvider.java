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
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLLineMarkerProvider extends RelatedItemLineMarkerProvider {

	@Override
	public void collectNavigationMarkers(
		@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result,
		boolean forNavigation) {

		elements.stream(
		).forEach(
			element -> {
				Project project = element.getProject();

				Optional<XmlAttribute> nameXmlAttribute = Optional.of(
					element
				).filter(
					XmlToken.class::isInstance
				).map(
					XmlToken.class::cast
				).filter(
					xmlToken -> XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN.equals(xmlToken.getTokenType())
				).map(
					xmlToken -> PsiTreeUtil.getParentOfType(xmlToken, XmlAttribute.class)
				).filter(
					xmlAttribute -> Objects.equals("name", xmlAttribute.getLocalName())
				);

				nameXmlAttribute.map(
					xmlAttribute -> PsiTreeUtil.getParentOfType(xmlAttribute, XmlTag.class)
				).map(
					PsiElement::getParent
				).filter(
					XmlTag.class::isInstance
				).map(
					XmlTag.class::cast
				).filter(
					parentXmlTag -> Objects.equals("service-builder", parentXmlTag.getLocalName())
				).ifPresent(
					serviceBuilderXmlTag -> {
						XmlAttribute xmlAttribute = nameXmlAttribute.get();

						String entityName = xmlAttribute.getValue();

						String packagePath = serviceBuilderXmlTag.getAttributeValue("package-path");

						if ((entityName != null) && (packagePath != null)) {
							String targetClassName = packagePath + ".model.impl." + entityName + "Impl";

							JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);

							PsiClass psiClass = javaPsiFacade.findClass(
								targetClassName, GlobalSearchScope.allScope(project));

							if (psiClass != null) {
								NavigationGutterIconBuilder<PsiElement> navigationGutterIconBuilder =
									NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementedMethod);

								navigationGutterIconBuilder.setTargets(Collections.singletonList(psiClass));
								navigationGutterIconBuilder.setTooltipText("Navigate to Implementation");

								result.add(navigationGutterIconBuilder.createLineMarkerInfo(element));
							}
						}
					}
				);
			}
		);
	}

}