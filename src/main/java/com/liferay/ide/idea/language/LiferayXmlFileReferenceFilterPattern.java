/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language;

import com.intellij.psi.PsiElement;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayXmlFileReferenceFilterPattern extends FilterPattern {

	public LiferayXmlFileReferenceFilterPattern() {
		super(_createElementFilter());
	}

	private static ElementFilter _createElementFilter() {
		return new ElementFilter() {

			@Override
			public boolean isAcceptable(Object element, @Nullable PsiElement context) {
				if (element instanceof XmlElement) {
					XmlElement xmlElement = (XmlElement)element;

					PsiElement psiElement = xmlElement.getParent();

					if (psiElement instanceof XmlText) {
						XmlText xmlText = (XmlText)psiElement;

						XmlTag xmlTag = xmlText.getParentTag();

						if (xmlTag != null) {
							String namespace = xmlTag.getNamespace();

							if (_tagsMap.containsKey(namespace)) {
								Collection<String> tags = _tagsMap.get(namespace);

								if (tags.contains(xmlTag.getLocalName())) {
									return true;
								}
							}
						}
					}
					else if (xmlElement instanceof XmlAttributeValue) {
						if (psiElement instanceof XmlAttribute) {
							XmlAttributeValue xmlAttributeValue = (XmlAttributeValue)element;

							XmlAttribute xmlAttribute = (XmlAttribute)xmlAttributeValue.getParent();

							XmlTag xmlTag = xmlAttribute.getParent();

							if (xmlTag != null) {
								String namespace = xmlTag.getNamespace();

								if (_attributesMap.containsKey(namespace)) {
									Collection<AbstractMap.SimpleImmutableEntry<String, String>> pairs =
										_attributesMap.get(namespace);

									AbstractMap.SimpleImmutableEntry<String, String> pair =
										new AbstractMap.SimpleImmutableEntry<>(
											xmlTag.getLocalName(), xmlAttribute.getLocalName());

									if (pairs.contains(pair)) {
										return true;
									}
								}
							}
						}
					}
				}

				return false;
			}

			@Override
			@SuppressWarnings("rawtypes")
			public boolean isClassAcceptable(Class hintClass) {
				return true;
			}

		};
	}

	private static final Map<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>> _attributesMap =
		new HashMap<>();

	private static final Map<String, Collection<String>> _tagsMap = new HashMap<>() {
		{
			put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_HOOK_7_0_0,
				Arrays.asList("portal-properties", "language-properties", "custom-jsp-dir"));
			put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_HOOK_7_1_0,
				Arrays.asList("portal-properties", "language-properties", "custom-jsp-dir"));
			put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_0_0,
				Arrays.asList("template-path", "wap-template-path", "thumbnail-path", "screenshot-path"));
			put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_1_0,
				Arrays.asList("template-path", "wap-template-path", "thumbnail-path", "screenshot-path"));
			put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_0_0,
				Arrays.asList(
					"root-path", "templates-path", "css-path", "images-path", "javascript-path",
					"color-scheme-images-path", "template-path", "wap-template-path", "thumbnail-path",
					"portlet-decorator-thumbnail-path"));
			put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_1_0,
				Arrays.asList(
					"root-path", "templates-path", "css-path", "images-path", "javascript-path",
					"color-scheme-images-path", "template-path", "wap-template-path", "thumbnail-path",
					"portlet-decorator-thumbnail-path"));
			put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_PORTLET_APP_7_0_0,
				Arrays.asList(
					"friendly-url-routes", "header-portal-css", "header-portlet-css", "header-portal-javascript",
					"header-portlet-javascript", "footer-portal-css", "footer-portlet-css", "footer-portal-javascript",
					"footer-portlet-javascript", "icon", "user-notification-definitions"));
			put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_PORTLET_APP_7_1_0,
				Arrays.asList(
					"friendly-url-routes", "header-portal-css", "header-portlet-css", "header-portal-javascript",
					"header-portlet-javascript", "footer-portal-css", "footer-portlet-css", "footer-portal-javascript",
					"footer-portlet-javascript", "icon", "user-notification-definitions"));
			_attributesMap.put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_0_0,
				List.of(new SimpleImmutableEntry<>("resource", "file")));
			_attributesMap.put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_1_0,
				_attributesMap.get(
					LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_0_0));

			_attributesMap.put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_0_0,
				List.of(new SimpleImmutableEntry<>("service-builder-import", "file")));
			_attributesMap.put(
				LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_1_0,
				_attributesMap.get(LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_0_0));

			put(LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_THEME_LOADER_7_0_0, List.of("themes-path"));
			put(LiferayDefinitionsResourceProvider.XML_NAMESPACE_LIFERAY_THEME_LOADER_7_1_0, List.of("themes-path"));
		}
	};

}