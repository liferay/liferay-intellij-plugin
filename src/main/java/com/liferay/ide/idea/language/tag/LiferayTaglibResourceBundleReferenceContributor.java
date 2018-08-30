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
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.XmlUtil;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * Adds code completion features for references to language keys inside Liferay or AlloyUI Taglibs
 *
 * @author Dominik Marks
 */
public class LiferayTaglibResourceBundleReferenceContributor extends PsiReferenceContributor {

	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
		Set<String> attributeNames = new HashSet<>();

		_taglibAttributes.forEach(
			(key, value) -> {
				Stream<SimpleImmutableEntry<String, String>> stream = value.stream();

				stream.map(
					SimpleImmutableEntry::getValue
				).forEach(
					attributeNames::add
				);
			});

		XmlUtil.registerXmlAttributeValueReferenceProvider(
			registrar, attributeNames.toArray(new String[attributeNames.size()]), new LiferayTaglibFilter(), true,
			new LiferayTaglibResourceBundleReferenceProvider(true));
	}

	private static Map<String, Collection<SimpleImmutableEntry<String, String>>> _taglibAttributes = new HashMap<>();

	static {
		_taglibAttributes.put(
			LiferayTaglibs.TAGLIB_URI_LIFERAY_UI,
			Arrays.asList(
				new SimpleImmutableEntry<>("app-view-search-entry", "containerType"),
				new SimpleImmutableEntry<>("alert", "message"),
				new SimpleImmutableEntry<>("asset-addon-entry-selector", "title"),
				new SimpleImmutableEntry<>("asset-metadata", "metadataField"),
				new SimpleImmutableEntry<>("asset-tags-summary", "message"),
				new SimpleImmutableEntry<>("custom-attribute", "name"),
				new SimpleImmutableEntry<>("diff-html", "infoMessage"),
				new SimpleImmutableEntry<>("drop-here-info", "message"),
				new SimpleImmutableEntry<>("empty-result-message", "message"),
				new SimpleImmutableEntry<>("error", "message"),
				new SimpleImmutableEntry<>("form-navigator", "categoryLabels"),
				new SimpleImmutableEntry<>("form-navigator", "categorySectionLabels"),
				new SimpleImmutableEntry<>("header", "backLabel"), new SimpleImmutableEntry<>("header", "title"),
				new SimpleImmutableEntry<>("icon", "message"),
				new SimpleImmutableEntry<>("icon-delete", "confirmation"),
				new SimpleImmutableEntry<>("icon-delete", "message"),
				new SimpleImmutableEntry<>("icon-help", "message"), new SimpleImmutableEntry<>("icon-menu", "message"),
				new SimpleImmutableEntry<>("input-field", "placeholder"),
				new SimpleImmutableEntry<>("input-localized", "helpMessaage"),
				new SimpleImmutableEntry<>("input-localized", "placeholder"),
				new SimpleImmutableEntry<>("input-resource", "title"),
				new SimpleImmutableEntry<>("input-move-boxes", "leftTitle"),
				new SimpleImmutableEntry<>("input-move-boxes", "rightTitle"),
				new SimpleImmutableEntry<>("input-resource", "title"), new SimpleImmutableEntry<>("message", "key"),
				new SimpleImmutableEntry<>("panel", "helpMessage"), new SimpleImmutableEntry<>("panel", "title"),
				new SimpleImmutableEntry<>("progress", "message"),
				new SimpleImmutableEntry<>("quick-access-entry", "label"),
				new SimpleImmutableEntry<>("search-container", "emptyResultsMessage"),
				new SimpleImmutableEntry<>("search-container", "headerNames"),
				new SimpleImmutableEntry<>("search-container-column-button", "name"),
				new SimpleImmutableEntry<>("search-container-column-date", "name"),
				new SimpleImmutableEntry<>("search-container-column-text", "name"),
				new SimpleImmutableEntry<>("search-toggle", "buttonLabel"),
				new SimpleImmutableEntry<>("success", "message"), new SimpleImmutableEntry<>("tabs", "names"),
				new SimpleImmutableEntry<>("upload-progress", "message")));

		_taglibAttributes.put(
			LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI,
			Arrays.asList(
				new SimpleImmutableEntry<>("a", "title"), new SimpleImmutableEntry<>("a", "label"),
				new SimpleImmutableEntry<>("button", "value"),
				new SimpleImmutableEntry<>("field-wrapper", "helpMessage"),
				new SimpleImmutableEntry<>("field-wrapper", "label"),
				new SimpleImmutableEntry<>("fieldset", "helpMessage"), new SimpleImmutableEntry<>("fieldset", "label"),
				new SimpleImmutableEntry<>("icon", "label"), new SimpleImmutableEntry<>("input", "helpMessage"),
				new SimpleImmutableEntry<>("input", "label"), new SimpleImmutableEntry<>("input", "labelOff"),
				new SimpleImmutableEntry<>("input", "labelOn"), new SimpleImmutableEntry<>("input", "title"),
				new SimpleImmutableEntry<>("input", "placeholder"), new SimpleImmutableEntry<>("input", "prefix"),
				new SimpleImmutableEntry<>("input", "suffix"),
				new SimpleImmutableEntry<>("nav-bar", "selectedItemName"),
				new SimpleImmutableEntry<>("nav-item", "label"), new SimpleImmutableEntry<>("nav-item", "title"),
				new SimpleImmutableEntry<>("option", "label"), new SimpleImmutableEntry<>("panel", "label"),
				new SimpleImmutableEntry<>("select", "label"), new SimpleImmutableEntry<>("select", "helpMessage"),
				new SimpleImmutableEntry<>("select", "prefix"), new SimpleImmutableEntry<>("select", "suffix"),
				new SimpleImmutableEntry<>("select", "title"), new SimpleImmutableEntry<>("validator", "errorMessage"),
				new SimpleImmutableEntry<>("workflow-status", "helpMessage"),
				new SimpleImmutableEntry<>("workflow-status", "statusMessage")));

		_taglibAttributes.put(
			LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI_OLD, _taglibAttributes.get(LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI));

		_taglibAttributes.put(
			LiferayTaglibs.TAGLIB_URI_LIFERAY_ASSET,
			Arrays.asList(
				new SimpleImmutableEntry<>("asset-addon-entry-selector", "title"),
				new SimpleImmutableEntry<>("asset-metadata", "metadataField"),
				new SimpleImmutableEntry<>("asset-tags-summary", "message")));

		_taglibAttributes.put(
			LiferayTaglibs.TAGLIB_URI_LIFERAY_EXPANDO,
			Arrays.asList(new SimpleImmutableEntry<>("custom-attribute", "name")));

		_taglibAttributes.put(
			LiferayTaglibs.TAGLIB_URI_LIFERAY_FRONTEND,
			Arrays.asList(
				new SimpleImmutableEntry<>("email-notification-settings", "bodyLabel"),
				new SimpleImmutableEntry<>("email-notification-settings", "helpMessage"),
				new SimpleImmutableEntry<>("management-bar-button", "label"),
				new SimpleImmutableEntry<>("management-bar-filter", "label")));

		_taglibAttributes.put(
			LiferayTaglibs.TAGLIB_URI_LIFERAY_TRASH,
			Arrays.asList(
				new SimpleImmutableEntry<>("empty", "confirmMessage"),
				new SimpleImmutableEntry<>("empty", "emptyMessage"),
				new SimpleImmutableEntry<>("empty", "infoMessage")));
	}

	private class LiferayTaglibFilter implements ElementFilter {

		@Override
		public boolean isAcceptable(Object element, PsiElement context) {
			PsiElement psiElement = (PsiElement)element;

			PsiElement parent = psiElement.getParent();

			if (parent instanceof XmlAttribute) {
				XmlAttribute xmlAttribute = (XmlAttribute)parent;

				XmlTag xmlTag = xmlAttribute.getParent();

				if (xmlTag != null) {
					String namespace = xmlTag.getNamespace();

					if (_taglibAttributes.containsKey(namespace)) {
						String attributeLocalName = xmlAttribute.getLocalName();
						String tagLocalName = xmlTag.getLocalName();
						Collection<SimpleImmutableEntry<String, String>> entries = _taglibAttributes.get(namespace);

						Stream<SimpleImmutableEntry<String, String>> stream = entries.stream();

						boolean anyMatch = stream.anyMatch(
							entry -> {
								String key = entry.getKey();
								String value = entry.getValue();

								if (key.equals(tagLocalName) && value.equals(attributeLocalName)) {
									return true;
								}

								return false;
							});

						if (anyMatch) {
							return true;
						}

						return false;
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

	}

}