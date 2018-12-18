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

package com.liferay.ide.idea.language.tag;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibClassNameReferenceContributor extends AbstractLiferayTaglibReferenceContributor {

	@Override
	protected String[] getAttributeNames() {
		return new String[] {
			"assetCategoryClassName", "assetTagClassName", "className", "exception", "model", "modelResource",
			"portletProviderClassName"
		};
	}

	@Override
	protected PsiReferenceProvider getReferenceProvider() {
		JavaClassReferenceProvider provider = new JavaClassReferenceProvider();

		provider.setOption(JavaClassReferenceProvider.ADVANCED_RESOLVE, Boolean.TRUE);
		provider.setOption(JavaClassReferenceProvider.RESOLVE_QUALIFIED_CLASS_NAME, Boolean.TRUE);

		return provider;
	}

	@Override
	protected boolean isSuitableAttribute(XmlAttribute xmlAttribute) {
		if (_containsTextOnly(xmlAttribute)) {
			XmlTag xmlTag = xmlAttribute.getParent();

			if (xmlTag != null) {
				String namespace = xmlTag.getNamespace();
				String xmlTagLocalName = xmlTag.getLocalName();
				String xmlAttributeLocalName = xmlAttribute.getLocalName();

				if (_taglibAttributes.containsKey(namespace)) {
					Collection<AbstractMap.SimpleImmutableEntry<String, String>> entries = _taglibAttributes.get(
						namespace);

					Stream<AbstractMap.SimpleImmutableEntry<String, String>> entriesStream = entries.stream();

					return entriesStream.anyMatch(
						entry -> {
							String key = entry.getKey();
							String value = entry.getValue();

							if (key.equals(xmlTagLocalName) && value.equals(xmlAttributeLocalName)) {
								return true;
							}

							return false;
						});
				}
			}
		}

		return false;
	}

	private boolean _containsTextOnly(XmlAttribute xmlAttribute) {
		return Stream.of(
			xmlAttribute
		).map(
			XmlAttribute::getValueElement
		).filter(
			Objects::nonNull
		).map(
			PsiElement::getChildren
		).flatMap(
			Stream::of
		).filter(
			child -> child instanceof XmlToken
		).map(
			xmlToken -> (XmlToken)xmlToken
		).map(
			XmlToken::getTokenType
		).anyMatch(
			XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN::equals
		);
	}

	private static Map<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>> _taglibAttributes =
		new HashMap<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>>() {
			{
				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI,
					Arrays.asList(
						new SimpleImmutableEntry<>("model-context", "model"),
						new SimpleImmutableEntry<>("input", "model"), new SimpleImmutableEntry<>("select", "model"),
						new SimpleImmutableEntry<>("workflow-status", "model")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_EXPANDO,
					Arrays.asList(
						new SimpleImmutableEntry<>("custom-attribute", "className"),
						new SimpleImmutableEntry<>("custom-attribute-list", "className"),
						new SimpleImmutableEntry<>("custom-attributes-available", "className")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_FLAGS,
					Arrays.asList(new SimpleImmutableEntry<>("flags", "className")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_PORTLET,
					Arrays.asList(new SimpleImmutableEntry<>("runtime", "portletProviderClassName")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_SECURITY,
					Arrays.asList(new SimpleImmutableEntry<>("permissionsURL", "modelResource")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_UI,
					Arrays.asList(
						new SimpleImmutableEntry<>("app-view-entry", "assetCategoryClassName"),
						new SimpleImmutableEntry<>("app-view-entry", "assetTagClassName"),
						new SimpleImmutableEntry<>("asset-categories-available", "className"),
						new SimpleImmutableEntry<>("asset-categories-selector", "className"),
						new SimpleImmutableEntry<>("asset-categories-summary", "className"),
						new SimpleImmutableEntry<>("asset-display", "className"),
						new SimpleImmutableEntry<>("asset-links", "className"),
						new SimpleImmutableEntry<>("asset-metadata", "className"),
						new SimpleImmutableEntry<>("asset-tags-available", "className"),
						new SimpleImmutableEntry<>("asset-tags-selector", "className"),
						new SimpleImmutableEntry<>("asset-tags-summary", "className"),
						new SimpleImmutableEntry<>("discussion", "className"),
						new SimpleImmutableEntry<>("error", "exception"),
						new SimpleImmutableEntry<>("input-asset-links", "className"),
						new SimpleImmutableEntry<>("input-field", "model"),
						new SimpleImmutableEntry<>("input-permissions", "modelName"),
						new SimpleImmutableEntry<>("input-permissions-params", "modelName"),
						new SimpleImmutableEntry<>("ratings", "className"),
						new SimpleImmutableEntry<>("search-container-row", "className"),
						new SimpleImmutableEntry<>("social-activities", "className")));
			}
		};

}