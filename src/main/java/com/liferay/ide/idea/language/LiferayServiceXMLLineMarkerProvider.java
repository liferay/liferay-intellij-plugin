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
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLLineMarkerProvider extends RelatedItemLineMarkerProvider {

	@Override
	protected void collectNavigationMarkers(
		@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

		XmlAttribute nameXmlAttribute = Stream.of(
			element
		).filter(
			xmlToken -> xmlToken instanceof XmlToken
		).map(
			xmlToken -> (XmlToken)xmlToken
		).filter(
			xmlToken -> XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN.equals(xmlToken.getTokenType())
		).map(
			xmlAttribute -> PsiTreeUtil.getParentOfType(xmlAttribute, XmlAttribute.class)
		).filter(
			Objects::nonNull
		).filter(
			xmlAttribute -> "name".equals(xmlAttribute.getLocalName())
		).findFirst(
		).orElse(
			null
		);

		if (nameXmlAttribute != null) {
			XmlTag serviceBuilderXmlTag = Stream.of(
				nameXmlAttribute
			).map(
				xmlTag -> PsiTreeUtil.getParentOfType(xmlTag, XmlTag.class)
			).filter(
				Objects::nonNull
			).map(
				PsiElement::getParent
			).filter(
				parentXmlTag -> parentXmlTag instanceof XmlTag
			).map(
				parentXmlTag -> (XmlTag)parentXmlTag
			).filter(
				parentXmlTag -> "service-builder".equals(parentXmlTag.getLocalName())
			).findFirst(
			).orElse(
				null
			);

			if (serviceBuilderXmlTag != null) {
				String packagePath = serviceBuilderXmlTag.getAttributeValue("package-path");
				String entityName = nameXmlAttribute.getValue();

				if ((packagePath != null) && (entityName != null)) {
					Project project = element.getProject();

					String targetClassName = packagePath + ".model.impl." + entityName + "Impl";

					JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);

					PsiClass psiClass = javaPsiFacade.findClass(targetClassName, GlobalSearchScope.allScope(project));

					if (psiClass != null) {
						NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(
							AllIcons.Gutter.ImplementedMethod);

						builder.setTargets(Collections.singletonList(psiClass));
						builder.setTooltipText("Navigate to Implementation");

						result.add(builder.createLineMarkerInfo(element));
					}
				}
			}
		}
	}

}