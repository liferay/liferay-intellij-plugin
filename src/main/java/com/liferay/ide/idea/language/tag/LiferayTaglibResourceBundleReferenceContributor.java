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

import java.util.AbstractMap;
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
				Stream<AbstractMap.SimpleImmutableEntry<String, String>> stream = value.stream();

				stream.map(
					AbstractMap.SimpleImmutableEntry::getValue
				).forEach(
					attributeNames::add
				);
			});

		XmlUtil.registerXmlAttributeValueReferenceProvider(
			registrar, attributeNames.toArray(new String[0]), new LiferayTaglibFilter(), true,
			new LiferayTaglibResourceBundleReferenceProvider(true));
	}

	@SuppressWarnings("serial")
	private static Map<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>> _taglibAttributes =
		new HashMap<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>>() {
			{
				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_ASSET,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("asset-addon-entry-selector", "title"),
						new AbstractMap.SimpleImmutableEntry<>("asset-metadata", "metadataField"),
						new AbstractMap.SimpleImmutableEntry<>("asset-tags-summary", "message")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("a", "title"),
						new AbstractMap.SimpleImmutableEntry<>("a", "label"),
						new AbstractMap.SimpleImmutableEntry<>("button", "value"),
						new AbstractMap.SimpleImmutableEntry<>("field-wrapper", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("field-wrapper", "label"),
						new AbstractMap.SimpleImmutableEntry<>("fieldset", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("fieldset", "label"),
						new AbstractMap.SimpleImmutableEntry<>("icon", "label"),
						new AbstractMap.SimpleImmutableEntry<>("input", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("input", "label"),
						new AbstractMap.SimpleImmutableEntry<>("input", "labelOff"),
						new AbstractMap.SimpleImmutableEntry<>("input", "labelOn"),
						new AbstractMap.SimpleImmutableEntry<>("input", "title"),
						new AbstractMap.SimpleImmutableEntry<>("input", "placeholder"),
						new AbstractMap.SimpleImmutableEntry<>("input", "prefix"),
						new AbstractMap.SimpleImmutableEntry<>("input", "suffix"),
						new AbstractMap.SimpleImmutableEntry<>("nav-bar", "selectedItemName"),
						new AbstractMap.SimpleImmutableEntry<>("nav-item", "label"),
						new AbstractMap.SimpleImmutableEntry<>("nav-item", "title"),
						new AbstractMap.SimpleImmutableEntry<>("option", "label"),
						new AbstractMap.SimpleImmutableEntry<>("panel", "label"),
						new AbstractMap.SimpleImmutableEntry<>("select", "label"),
						new AbstractMap.SimpleImmutableEntry<>("select", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("select", "prefix"),
						new AbstractMap.SimpleImmutableEntry<>("select", "suffix"),
						new AbstractMap.SimpleImmutableEntry<>("select", "title"),
						new AbstractMap.SimpleImmutableEntry<>("validator", "errorMessage"),
						new AbstractMap.SimpleImmutableEntry<>("workflow-status", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("workflow-status", "statusMessage")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI_OLD,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("a", "title"),
						new AbstractMap.SimpleImmutableEntry<>("a", "label"),
						new AbstractMap.SimpleImmutableEntry<>("button", "value"),
						new AbstractMap.SimpleImmutableEntry<>("field-wrapper", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("field-wrapper", "label"),
						new AbstractMap.SimpleImmutableEntry<>("fieldset", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("fieldset", "label"),
						new AbstractMap.SimpleImmutableEntry<>("icon", "label"),
						new AbstractMap.SimpleImmutableEntry<>("input", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("input", "label"),
						new AbstractMap.SimpleImmutableEntry<>("input", "labelOff"),
						new AbstractMap.SimpleImmutableEntry<>("input", "labelOn"),
						new AbstractMap.SimpleImmutableEntry<>("input", "title"),
						new AbstractMap.SimpleImmutableEntry<>("input", "placeholder"),
						new AbstractMap.SimpleImmutableEntry<>("input", "prefix"),
						new AbstractMap.SimpleImmutableEntry<>("input", "suffix"),
						new AbstractMap.SimpleImmutableEntry<>("nav-bar", "selectedItemName"),
						new AbstractMap.SimpleImmutableEntry<>("nav-item", "label"),
						new AbstractMap.SimpleImmutableEntry<>("nav-item", "title"),
						new AbstractMap.SimpleImmutableEntry<>("option", "label"),
						new AbstractMap.SimpleImmutableEntry<>("panel", "label"),
						new AbstractMap.SimpleImmutableEntry<>("select", "label"),
						new AbstractMap.SimpleImmutableEntry<>("select", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("select", "prefix"),
						new AbstractMap.SimpleImmutableEntry<>("select", "suffix"),
						new AbstractMap.SimpleImmutableEntry<>("select", "title"),
						new AbstractMap.SimpleImmutableEntry<>("validator", "errorMessage"),
						new AbstractMap.SimpleImmutableEntry<>("workflow-status", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("workflow-status", "statusMessage")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_DDM,
					Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("template-selector", "label")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_EXPANDO,
					Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("custom-attribute", "name")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_FRONTEND,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("add-menu-item", "title"),
						new AbstractMap.SimpleImmutableEntry<>("email-notification-settings", "bodyLabel"),
						new AbstractMap.SimpleImmutableEntry<>("email-notification-settings", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("fieldset", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("fieldset", "label"),
						new AbstractMap.SimpleImmutableEntry<>("management-bar-button", "label"),
						new AbstractMap.SimpleImmutableEntry<>("management-bar-filter", "label")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_ITEM_SELECTOR,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("repository-entry-browser", "emptyResultsMessage")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_PRODUCT_NAVIGATION,
					Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("personal-menu", "label")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_SITE,
					Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("site-browser", "emptyResultsMessage")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_STAGING,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("checkbox", "description"),
						new AbstractMap.SimpleImmutableEntry<>("checkbox", "label"),
						new AbstractMap.SimpleImmutableEntry<>("checkbox", "popover"),
						new AbstractMap.SimpleImmutableEntry<>("checkbox", "suggestion"),
						new AbstractMap.SimpleImmutableEntry<>("checkbox", "warning"),
						new AbstractMap.SimpleImmutableEntry<>("configuration-header", "label"),
						new AbstractMap.SimpleImmutableEntry<>("popover", "text"),
						new AbstractMap.SimpleImmutableEntry<>("popover", "title"),
						new AbstractMap.SimpleImmutableEntry<>("process-date", "labelKey"),
						new AbstractMap.SimpleImmutableEntry<>("process-list", "emptyResultsMessage"),
						new AbstractMap.SimpleImmutableEntry<>("radio", "description"),
						new AbstractMap.SimpleImmutableEntry<>("radio", "label"),
						new AbstractMap.SimpleImmutableEntry<>("radio", "popover")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_TRASH,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("empty", "confirmMessage"),
						new AbstractMap.SimpleImmutableEntry<>("empty", "emptyMessage"),
						new AbstractMap.SimpleImmutableEntry<>("empty", "infoMessage")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_UI,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("app-view-search-entry", "containerType"),
						new AbstractMap.SimpleImmutableEntry<>("alert", "message"),
						new AbstractMap.SimpleImmutableEntry<>("asset-addon-entry-selector", "title"),
						new AbstractMap.SimpleImmutableEntry<>("asset-metadata", "metadataField"),
						new AbstractMap.SimpleImmutableEntry<>("asset-tags-summary", "message"),
						new AbstractMap.SimpleImmutableEntry<>("custom-attribute", "name"),
						new AbstractMap.SimpleImmutableEntry<>("diff-html", "infoMessage"),
						new AbstractMap.SimpleImmutableEntry<>("drop-here-info", "message"),
						new AbstractMap.SimpleImmutableEntry<>("empty-result-message", "message"),
						new AbstractMap.SimpleImmutableEntry<>("error", "message"),
						new AbstractMap.SimpleImmutableEntry<>("form-navigator", "categoryLabels"),
						new AbstractMap.SimpleImmutableEntry<>("form-navigator", "categorySectionLabels"),
						new AbstractMap.SimpleImmutableEntry<>("header", "backLabel"),
						new AbstractMap.SimpleImmutableEntry<>("header", "title"),
						new AbstractMap.SimpleImmutableEntry<>("icon", "message"),
						new AbstractMap.SimpleImmutableEntry<>("icon-delete", "confirmation"),
						new AbstractMap.SimpleImmutableEntry<>("icon-delete", "message"),
						new AbstractMap.SimpleImmutableEntry<>("icon-help", "message"),
						new AbstractMap.SimpleImmutableEntry<>("icon-menu", "message"),
						new AbstractMap.SimpleImmutableEntry<>("icon-menu", "triggerLabel"),
						new AbstractMap.SimpleImmutableEntry<>("input-date", "dateTogglerCheckboxLabel"),
						new AbstractMap.SimpleImmutableEntry<>("input-field", "placeholder"),
						new AbstractMap.SimpleImmutableEntry<>("input-localized", "helpMessaage"),
						new AbstractMap.SimpleImmutableEntry<>("input-localized", "placeholder"),
						new AbstractMap.SimpleImmutableEntry<>("input-resource", "title"),
						new AbstractMap.SimpleImmutableEntry<>("input-move-boxes", "leftTitle"),
						new AbstractMap.SimpleImmutableEntry<>("input-move-boxes", "rightTitle"),
						new AbstractMap.SimpleImmutableEntry<>("input-resource", "title"),
						new AbstractMap.SimpleImmutableEntry<>("message", "key"),
						new AbstractMap.SimpleImmutableEntry<>("panel", "helpMessage"),
						new AbstractMap.SimpleImmutableEntry<>("panel", "title"),
						new AbstractMap.SimpleImmutableEntry<>("progress", "message"),
						new AbstractMap.SimpleImmutableEntry<>("quick-access-entry", "label"),
						new AbstractMap.SimpleImmutableEntry<>("search-container", "emptyResultsMessage"),
						new AbstractMap.SimpleImmutableEntry<>("search-container", "headerNames"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-column-button", "name"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-column-date", "name"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-column-text", "name"),
						new AbstractMap.SimpleImmutableEntry<>("search-toggle", "buttonLabel"),
						new AbstractMap.SimpleImmutableEntry<>("success", "message"),
						new AbstractMap.SimpleImmutableEntry<>("tabs", "names"),
						new AbstractMap.SimpleImmutableEntry<>("upload-progress", "message")));
			}
		};

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

						Collection<AbstractMap.SimpleImmutableEntry<String, String>> entries = _taglibAttributes.get(
							namespace);

						Stream<AbstractMap.SimpleImmutableEntry<String, String>> stream = entries.stream();

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