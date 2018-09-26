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

import java.util.AbstractMap.SimpleImmutableEntry;
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
		"cssClass", "iconCssClass", "imageCssClass", "imageCSSClass", "linkCssClass", "triggerCssClass",
		"elementClasses", "draggableImage", "emptyResultsMessageCssClass", "userIconCssClass", "helpTextCssClass",
		"wrapperCssClass", "anchorCssClass", "userIconCssClass", "checkboxCSSClass");

	private static Map<String, Collection<SimpleImmutableEntry<String, String>>> _taglibAttributes =
		new HashMap<String, Collection<SimpleImmutableEntry<String, String>>>() {
			{
				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI,
					Arrays.asList(
						new SimpleImmutableEntry<>("a", "cssClass"), new SimpleImmutableEntry<>("a", "iconCssClass"),
						new SimpleImmutableEntry<>("alert", "cssClass"),
						new SimpleImmutableEntry<>("button", "cssClass"),
						new SimpleImmutableEntry<>("button-row", "cssClass"),
						new SimpleImmutableEntry<>("col", "cssClass"),
						new SimpleImmutableEntry<>("container", "cssClass"),
						new SimpleImmutableEntry<>("field-wrapper", "cssClass"),
						new SimpleImmutableEntry<>("fieldset", "cssClass"),
						new SimpleImmutableEntry<>("form", "cssClass"), new SimpleImmutableEntry<>("icon", "cssClass"),
						new SimpleImmutableEntry<>("input", "cssClass"),
						new SimpleImmutableEntry<>("input", "helpTextCssClass"),
						new SimpleImmutableEntry<>("input", "wrapperCssClass"),
						new SimpleImmutableEntry<>("nav", "cssClass"),
						new SimpleImmutableEntry<>("nav-bar", "cssClass"),
						new SimpleImmutableEntry<>("nav-bar-search", "cssClass"),
						new SimpleImmutableEntry<>("nav-item", "anchorCssClass"),
						new SimpleImmutableEntry<>("nav-item", "cssClass"),
						new SimpleImmutableEntry<>("nav-item", "iconCssClass"),
						new SimpleImmutableEntry<>("option", "cssClass"), new SimpleImmutableEntry<>("row", "cssClass"),
						new SimpleImmutableEntry<>("select", "cssClass"),
						new SimpleImmutableEntry<>("select", "wrapperCssClass")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI_OLD,
					Arrays.asList(
						new SimpleImmutableEntry<>("a", "cssClass"), new SimpleImmutableEntry<>("a", "iconCssClass"),
						new SimpleImmutableEntry<>("alert", "cssClass"),
						new SimpleImmutableEntry<>("button", "cssClass"),
						new SimpleImmutableEntry<>("button-row", "cssClass"),
						new SimpleImmutableEntry<>("col", "cssClass"),
						new SimpleImmutableEntry<>("container", "cssClass"),
						new SimpleImmutableEntry<>("field-wrapper", "cssClass"),
						new SimpleImmutableEntry<>("fieldset", "cssClass"),
						new SimpleImmutableEntry<>("form", "cssClass"), new SimpleImmutableEntry<>("icon", "cssClass"),
						new SimpleImmutableEntry<>("input", "cssClass"),
						new SimpleImmutableEntry<>("input", "helpTextCssClass"),
						new SimpleImmutableEntry<>("input", "wrapperCssClass"),
						new SimpleImmutableEntry<>("nav", "cssClass"),
						new SimpleImmutableEntry<>("nav-bar", "cssClass"),
						new SimpleImmutableEntry<>("nav-bar-search", "cssClass"),
						new SimpleImmutableEntry<>("nav-item", "anchorCssClass"),
						new SimpleImmutableEntry<>("nav-item", "cssClass"),
						new SimpleImmutableEntry<>("nav-item", "iconCssClass"),
						new SimpleImmutableEntry<>("option", "cssClass"), new SimpleImmutableEntry<>("row", "cssClass"),
						new SimpleImmutableEntry<>("select", "cssClass"),
						new SimpleImmutableEntry<>("select", "wrapperCssClass")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_UI,
					Arrays.asList(
						new SimpleImmutableEntry<>("alert", "cssClass"),
						new SimpleImmutableEntry<>("app-view-entry", "cssClass"),
						new SimpleImmutableEntry<>("app-view-entry", "iconCssClass"),
						new SimpleImmutableEntry<>("app-view-search-entry", "iconCssClass"),
						new SimpleImmutableEntry<>("empty-result-message", "cssClass"),
						new SimpleImmutableEntry<>("header", "cssClass"),
						new SimpleImmutableEntry<>("icon", "cssClass"),
						new SimpleImmutableEntry<>("icon", "iconCssClass"),
						new SimpleImmutableEntry<>("icon", "linkCssClass"),
						new SimpleImmutableEntry<>("icon-delete", "cssClass"),
						new SimpleImmutableEntry<>("icon-delete", "linkCssClass"),
						new SimpleImmutableEntry<>("icon-menu", "cssClass"),
						new SimpleImmutableEntry<>("icon-menu", "triggerCssClass"),
						new SimpleImmutableEntry<>("input-checkbox", "cssClass"),
						new SimpleImmutableEntry<>("input-date", "cssClass"),
						new SimpleImmutableEntry<>("input-editor", "cssClass"),
						new SimpleImmutableEntry<>("input-field", "cssClass"),
						new SimpleImmutableEntry<>("input-localized", "cssClass"),
						new SimpleImmutableEntry<>("input-move-boxes", "cssClass"),
						new SimpleImmutableEntry<>("input-repeat", "cssClass"),
						new SimpleImmutableEntry<>("input-resource", "cssClass"),
						new SimpleImmutableEntry<>("input-search", "cssClass"),
						new SimpleImmutableEntry<>("input-select", "cssClass"),
						new SimpleImmutableEntry<>("input-textarea", "cssClass"),
						new SimpleImmutableEntry<>("input-time", "cssClass"),
						new SimpleImmutableEntry<>("input-time-zone", "cssClass"),
						new SimpleImmutableEntry<>("my-sites", "cssClass"),
						new SimpleImmutableEntry<>("panel", "cssClass"),
						new SimpleImmutableEntry<>("panel", "iconCssClass"),
						new SimpleImmutableEntry<>("panel-container", "cssClass"),
						new SimpleImmutableEntry<>("search-container", "cssClass"),
						new SimpleImmutableEntry<>("search-container", "emptyResultsMessageCssClass"),
						new SimpleImmutableEntry<>("search-container-column-button", "cssClass"),
						new SimpleImmutableEntry<>("search-container-column-date", "cssClass"),
						new SimpleImmutableEntry<>("search-container-column-icon", "cssClass"),
						new SimpleImmutableEntry<>("search-container-column-image", "cssClass"),
						new SimpleImmutableEntry<>("search-container-column-jsp", "cssClass"),
						new SimpleImmutableEntry<>("search-container-column-status", "cssClass"),
						new SimpleImmutableEntry<>("search-container-column-text", "cssClass"),
						new SimpleImmutableEntry<>("search-container-column-user", "cssClass"),
						new SimpleImmutableEntry<>("search-container-row", "cssClass"),
						new SimpleImmutableEntry<>("tabs", "cssClass"),
						new SimpleImmutableEntry<>("user-display", "imageCssClass"),
						new SimpleImmutableEntry<>("user-display", "userIconCssClass"),
						new SimpleImmutableEntry<>("user-portrait", "cssClass"),
						new SimpleImmutableEntry<>("user-portrait", "imageCssClass")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_FRONTEND,
					Arrays.asList(
						new SimpleImmutableEntry<>("horizontal-card", "checkboxCSSClass"),
						new SimpleImmutableEntry<>("horizontal-card", "cssClass"),
						new SimpleImmutableEntry<>("icon-vertical-card", "checkboxCSSClass"),
						new SimpleImmutableEntry<>("icon-vertical-card", "cssClass"),
						new SimpleImmutableEntry<>("image-card", "cssClass"),
						new SimpleImmutableEntry<>("image-card", "imageCssClass"),
						new SimpleImmutableEntry<>("info-bar-button", "cssClass"),
						new SimpleImmutableEntry<>("info-bar-button", "iconCssClass"),
						new SimpleImmutableEntry<>("info-bar-sidenav-toggler-button", "cssClass"),
						new SimpleImmutableEntry<>("info-bar-sidenav-toggler-button", "iconCssClass"),
						new SimpleImmutableEntry<>("management-bar-button", "cssClass"),
						new SimpleImmutableEntry<>("management-bar-button", "iconCssClass"),
						new SimpleImmutableEntry<>("management-bar-sidenav-toggler-button", "cssClass"),
						new SimpleImmutableEntry<>("management-bar-sidenav-toggler-button", "iconCssClass"),
						new SimpleImmutableEntry<>("translation-manager", "cssClass"),
						new SimpleImmutableEntry<>("user-vertical-card", "checkboxCSSClass"),
						new SimpleImmutableEntry<>("user-vertical-card", "cssClass"),
						new SimpleImmutableEntry<>("vertical-card", "checkboxCSSClass"),
						new SimpleImmutableEntry<>("vertical-card", "cssClass"),
						new SimpleImmutableEntry<>("vertical-card", "imageCSSClass"),
						new SimpleImmutableEntry<>("vertical-card-small-image", "cssClass")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_ITEM_SELECTOR,
					Arrays.asList(new SimpleImmutableEntry<>("image-selector", "draggableImage")));
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
				String xmlTagLocalName = xmlTag.getLocalName();
				String xmlAttributeLocalName = xmlAttribute.getLocalName();

				if (_taglibAttributes.containsKey(namespace)) {
					Collection<SimpleImmutableEntry<String, String>> attributes = _taglibAttributes.get(namespace);

					Stream<SimpleImmutableEntry<String, String>> attributesStream = attributes.stream();

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