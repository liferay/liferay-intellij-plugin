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

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JSTargetedInjector;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * Injects JavaScript language into Liferay specific taglibs (like <aui:script/> or <aui:a onClick=""/>)
 *
 * @author Dominik Marks
 */
public class LiferayTaglibJavascriptLanguageInjector implements JSTargetedInjector, MultiHostInjector {

	@NotNull
	@Override
	public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
		return Arrays.asList(XmlTag.class, XmlAttribute.class);
	}

	@Override
	public void getLanguagesToInject(@NotNull MultiHostRegistrar multiHostRegistrar, @NotNull PsiElement psiElement) {
		if (!psiElement.isValid()) {
			return;
		}

		XmlTag xmlTag;

		if (psiElement instanceof XmlTag) {
			xmlTag = (XmlTag)psiElement;
		}
		else {
			XmlAttribute xmlAttribute = (XmlAttribute)psiElement;

			xmlTag = xmlAttribute.getParent();
		}

		String localName = xmlTag.getLocalName();
		String namespace = xmlTag.getNamespace();

		if (_taglibAttributes.containsKey(namespace)) {
			Collection<AbstractMap.SimpleImmutableEntry<String, String>> attributes = _taglibAttributes.get(namespace);

			Stream<AbstractMap.SimpleImmutableEntry<String, String>> stream = attributes.stream();

			if (psiElement instanceof XmlTag) {
				stream.filter(
					attribute -> {
						String key = attribute.getKey();

						return key.equals(localName);
					}
				).filter(
					attribute -> Objects.equals("", attribute.getValue())
				).forEach(
					attribute -> _injectIntoBody(multiHostRegistrar, (XmlTag)psiElement)
				);
			}
			else {
				XmlAttribute xmlAttribute = (XmlAttribute)psiElement;

				String attributeName = xmlAttribute.getLocalName();

				stream.filter(
					attribute -> {
						String key = attribute.getKey();

						return key.equals(localName);
					}
				).filter(
					attribute -> {
						String value = attribute.getValue();

						return value.equals(attributeName);
					}
				).forEach(
					attribute -> _injectIntoAttribute(multiHostRegistrar, xmlAttribute)
				);
			}
		}
	}

	private void _injectIntoAttribute(MultiHostRegistrar multiHostRegistrar, XmlAttribute xmlAttribute) {
		Stream.of(
			xmlAttribute
		).map(
			XmlAttribute::getValueElement
		).filter(
			Objects::nonNull
		).map(
			XmlAttributeValue::getChildren
		).flatMap(
			psiElements -> Stream.of(psiElements)
		).filter(
			XmlToken.class::isInstance
		).map(
			xmlToken -> (XmlToken)xmlToken
		).map(
			XmlToken::getTokenType
		).filter(
			XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN::equals
		).findAny(
		).ifPresent(
			token -> {
				multiHostRegistrar.startInjecting(JavascriptLanguage.INSTANCE);
				multiHostRegistrar.addPlace(
					null, null, (PsiLanguageInjectionHost)xmlAttribute.getValueElement(),
					xmlAttribute.getValueTextRange());
				multiHostRegistrar.doneInjecting();
			}
		);
	}

	private void _injectIntoBody(MultiHostRegistrar multiHostRegistrar, XmlTag xmlTag) {
		Stream.of(
			xmlTag.getChildren()
		).filter(
			XmlText.class::isInstance
		).findAny(
		).ifPresent(
			t -> {
				String prefixWrapper = null;
				String suffixWrapper = null;

				if (LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI.equals(xmlTag.getNamespace()) &&
					Objects.equals("validator", xmlTag.getLocalName())) {

					String attributeValue = xmlTag.getAttributeValue("name");

					if (Objects.equals("custom", attributeValue) || Objects.equals("required", attributeValue)) {
						prefixWrapper = "(";
						suffixWrapper = ")();";
					}
					else {
						return;
					}
				}

				final String prefix = prefixWrapper;
				final String suffix = suffixWrapper;

				multiHostRegistrar.startInjecting(JavascriptLanguage.INSTANCE);

				Stream.of(
					xmlTag.getChildren()
				).filter(
					XmlText.class::isInstance
				).forEach(
					psiElement -> multiHostRegistrar.addPlace(
						prefix, suffix, (PsiLanguageInjectionHost)psiElement,
						new TextRange(0, psiElement.getTextLength()))
				);

				multiHostRegistrar.doneInjecting();
			}
		);
	}

	@SuppressWarnings("serial")
	private static Map<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>> _taglibAttributes =
		new HashMap<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>>() {
			{
				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("a", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("button", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("form", "onSubmit"),
						new AbstractMap.SimpleImmutableEntry<>("input", "onChange"),
						new AbstractMap.SimpleImmutableEntry<>("input", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("script", ""),
						new AbstractMap.SimpleImmutableEntry<>("select", "onChange"),
						new AbstractMap.SimpleImmutableEntry<>("select", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("validator", "")));
				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI_OLD,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("a", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("button", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("form", "onSubmit"),
						new AbstractMap.SimpleImmutableEntry<>("input", "onChange"),
						new AbstractMap.SimpleImmutableEntry<>("input", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("script", ""),
						new AbstractMap.SimpleImmutableEntry<>("select", "onChange"),
						new AbstractMap.SimpleImmutableEntry<>("select", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("validator", "")));
				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_FRONTEND,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("edit-form", "onSubmit"),
						new AbstractMap.SimpleImmutableEntry<>("html-vertical-card", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("icon-vertical-card", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("vertical-card", "onClick")));
				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_UI,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("icon", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("input-checkbox", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("input-move-boxes", "leftOnChange"),
						new AbstractMap.SimpleImmutableEntry<>("input-move-boxes", "rightOnChange"),
						new AbstractMap.SimpleImmutableEntry<>("page-iterator", "jsCall"),
						new AbstractMap.SimpleImmutableEntry<>("quick-access-entry", "onClick"),
						new AbstractMap.SimpleImmutableEntry<>("tabs", "onClick")));
			}
		};

}