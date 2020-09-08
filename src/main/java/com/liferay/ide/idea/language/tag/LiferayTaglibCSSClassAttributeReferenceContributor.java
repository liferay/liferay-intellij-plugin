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
import com.intellij.psi.css.CssSupportLoader;
import com.intellij.psi.css.impl.util.CssInHtmlClassOrIdReferenceProvider;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.XmlUtil;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * Adds support for attributes like &quot;cssClass&quot; and &quot;iconCssClass&quot; on Liferay and AlloyUI Tags, so that Code Completion for (S)CSS classes is available.
 *
 * @author Dominik Marks
 */
public class LiferayTaglibCSSClassAttributeReferenceContributor extends PsiReferenceContributor {

	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
		LiferayTaglibCssInHtmlClassOrIdReferenceProvider liferayTaglibCssInHtmlClassOrIdReferenceProvider =
			new LiferayTaglibCssInHtmlClassOrIdReferenceProvider();

		XmlUtil.registerXmlAttributeValueReferenceProvider(
			psiReferenceRegistrar, _attributeNames.toArray(new String[0]),
			liferayTaglibCssInHtmlClassOrIdReferenceProvider.getFilter(), false,
			liferayTaglibCssInHtmlClassOrIdReferenceProvider);
	}

	private static final List<String> _attributeNames = Arrays.asList(
		"anchorCssClass", "bodyClasses", "cardCssClass", "checkboxCSSClass", "class", "containerCssClass",
		"containerWrapperCssClass", "cssClass", "descriptionCSSClass", "draggableImage", "elementClasses",
		"emptyResultsMessageCssClass", "fieldSetCssClass", "headerClasses", "helpTextCssClass", "icon", "iconCssClass",
		"imageCssClass", "imageCSSClass", "labelCSSClass", "linkCssClass", "linkClass", "menubarCssClass",
		"navCssClass", "searchResultCssClass", "symbol", "triggerCssClass", "userIconCssClass", "wrapperCssClass");

	@SuppressWarnings("serial")
	private static Map<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>> _taglibAttributes =
		new HashMap<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>>() {
			{
				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_ADAPTIVE_MEDIA_IMAGE,
					Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("img", "class")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("a", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("a", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("alert", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("button", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("button-row", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("col", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("container", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("field-wrapper", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("fieldset", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("form", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input", "helpTextCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input", "wrapperCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav-bar", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav-bar-search", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav-item", "anchorCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav-item", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav-item", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("option", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("row", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("select", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("select", "wrapperCssClass")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI_OLD,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("a", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("a", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("alert", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("button", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("button-row", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("col", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("container", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("field-wrapper", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("fieldset", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("form", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input", "helpTextCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input", "wrapperCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav-bar", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav-bar-search", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav-item", "anchorCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav-item", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("nav-item", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("option", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("row", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("select", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("select", "wrapperCssClass")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_CLAY,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("alert", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("badge", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("button", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("button", "icon"),
						new AbstractMap.SimpleImmutableEntry<>("checkbox", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("dropdown-actions", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("dropdown-actions", "triggerCssClasses"),
						new AbstractMap.SimpleImmutableEntry<>("dropdown-menu", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("dropdown-menu", "icon"),
						new AbstractMap.SimpleImmutableEntry<>("dropdown-menu", "triggerCssClasses"),
						new AbstractMap.SimpleImmutableEntry<>("file-card", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("file-card", "icon"),
						new AbstractMap.SimpleImmutableEntry<>("horizontal-card", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("horizontal-card", "icon"),
						new AbstractMap.SimpleImmutableEntry<>("icon", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("icon", "symbol"),
						new AbstractMap.SimpleImmutableEntry<>("image-card", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("image-card", "icon"),
						new AbstractMap.SimpleImmutableEntry<>("label", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("link", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("link", "icon"),
						new AbstractMap.SimpleImmutableEntry<>("management-toolbar", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("navigation-bar", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("progressbar", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("radio", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("select", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("sticker", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("sticker", "icon"),
						new AbstractMap.SimpleImmutableEntry<>("stripe", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("user-card", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("user-card", "icon")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_DDM,
					Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("template-selector", "icon")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_DOCUMENT_LIBRARY,
					Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("mime-type-sticker", "cssClass")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_EDITOR,
					Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("editor", "cssClass")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_FRONTEND,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("add-menu-item", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("contextual-sidebar", "bodyClasses"),
						new AbstractMap.SimpleImmutableEntry<>("contextual-sidebar", "elementClasses"),
						new AbstractMap.SimpleImmutableEntry<>("contextual-sidebar", "headerClasses"),
						new AbstractMap.SimpleImmutableEntry<>("edit-form", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("fieldset", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("form-navigator", "fieldSetCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("horizontal-card", "cardCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("horizontal-card", "checkboxCSSClass"),
						new AbstractMap.SimpleImmutableEntry<>("horizontal-card", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("html-vertical-card", "cardCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("html-vertical-card", "checkboxCSSClass"),
						new AbstractMap.SimpleImmutableEntry<>("html-vertical-card", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon-vertical-card", "cardCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon-vertical-card", "checkboxCSSClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon-vertical-card", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("image-card", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("image-card", "imageCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("info-bar-button", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("info-bar-button", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("info-bar-sidenav-toggler-button", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("info-bar-sidenav-toggler-button", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("management-bar-button", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("management-bar-button", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("management-bar-sidenav-toggler-button", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("management-bar-sidenav-toggler-button", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("screen-navigation", "containerCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("screen-navigation", "containerWrapperCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("screen-navigation", "menubarCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("screen-navigation", "navCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("translation-manager", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("user-vertical-card", "cardCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("user-vertical-card", "checkboxCSSClass"),
						new AbstractMap.SimpleImmutableEntry<>("user-vertical-card", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("vertical-card", "cardCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("vertical-card", "checkboxCSSClass"),
						new AbstractMap.SimpleImmutableEntry<>("vertical-card", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("vertical-card", "imageCSSClass"),
						new AbstractMap.SimpleImmutableEntry<>("vertical-card-small-image", "cssClass")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_ITEM_SELECTOR,
					Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("image-selector", "draggableImage")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_JOURNAL,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("journal-article", "wrapperCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("journal-article-display", "wrapperCssClass")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_STAGING,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("menu", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("permissions", "descriptionCSSClass"),
						new AbstractMap.SimpleImmutableEntry<>("permissions", "labelCSSClass"),
						new AbstractMap.SimpleImmutableEntry<>("process-message-task-details", "linkClass"),
						new AbstractMap.SimpleImmutableEntry<>("process-status", "linkClass"),
						new AbstractMap.SimpleImmutableEntry<>("status", "cssClass")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_UI,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("alert", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("app-view-entry", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("app-view-entry", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("app-view-search-entry", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("empty-result-message", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("header", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon", "linkCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon-delete", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon-delete", "linkCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon-menu", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("icon-menu", "triggerCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-checkbox", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-date", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-editor", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-field", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-localized", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-move-boxes", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-repeat", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-resource", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-search", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-select", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-textarea", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-time", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("input-time-zone", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("my-sites", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("panel", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("panel", "iconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("panel-container", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-container", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-container", "emptyResultsMessageCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-column-button", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-column-date", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-column-icon", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-column-image", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-column-jsp", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-column-status", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-column-text", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-column-user", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-container-row", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("search-iterator", "searchResultCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("tabs", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("user-display", "imageCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("user-display", "userIconCssClass"),
						new AbstractMap.SimpleImmutableEntry<>("user-portrait", "cssClass"),
						new AbstractMap.SimpleImmutableEntry<>("user-portrait", "imageCssClass")));
			}
		};

	private class LiferayTaglibCssInHtmlClassOrIdReferenceProvider extends CssInHtmlClassOrIdReferenceProvider {

		@Override
		public ElementFilter getFilter() {
			return new LiferayTaglibFilter();
		}

		@Override
		protected boolean isSuitableAttribute(String attrName, XmlAttribute xmlAttribute) {
			XmlTag xmlTag = xmlAttribute.getParent();

			if (xmlTag != null) {
				String namespace = xmlTag.getNamespace();

				if (_taglibAttributes.containsKey(namespace)) {
					Collection<AbstractMap.SimpleImmutableEntry<String, String>> attributes = _taglibAttributes.get(
						namespace);

					Stream<AbstractMap.SimpleImmutableEntry<String, String>> attributesStream = attributes.stream();

					String xmlTagLocalName = xmlTag.getLocalName();

					String xmlAttributeLocalName = xmlAttribute.getLocalName();

					boolean anyMatch = attributesStream.anyMatch(
						attribute -> {
							String key = attribute.getKey();
							String value = attribute.getValue();

							if (key.equals(xmlTagLocalName) && value.equals(xmlAttributeLocalName)) {
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

			return false;
		}

		private class LiferayTaglibFilter implements ElementFilter {

			@Override
			public boolean isAcceptable(Object element, PsiElement psiElementContext) {
				PsiElement psiElement = (PsiElement)element;

				return Stream.of(
					psiElement
				).filter(
					CssSupportLoader::isInFileThatSupportsCssResolve
				).map(
					parent -> psiElement.getParent()
				).filter(
					parent -> parent instanceof XmlAttribute
				).map(
					xmlAttribute -> (XmlAttribute)xmlAttribute
				).anyMatch(
					xmlAttribute -> isSuitableAttribute(xmlAttribute.getName(), xmlAttribute)
				);
			}

			@Override
			@SuppressWarnings("rawtypes")
			public boolean isClassAcceptable(Class hintClass) {
				return true;
			}

		}

	}

}