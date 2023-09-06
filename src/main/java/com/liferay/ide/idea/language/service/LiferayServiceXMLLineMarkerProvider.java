/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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
					xmlAttribute -> Objects.equals(xmlAttribute.getLocalName(), "name")
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
					parentXmlTag -> Objects.equals(parentXmlTag.getLocalName(), "service-builder")
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