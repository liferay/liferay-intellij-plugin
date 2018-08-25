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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * Injects JavaScript language into Liferay specific taglibs (like <aui:script> or <aui:a onClick="">)
 *
 * @author Dominik Marks
 */
public class LiferayTaglibJavascriptLanguageInjector implements MultiHostInjector, JSTargetedInjector {

	@NotNull
	@Override
	public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
		return Arrays.asList(XmlTag.class, XmlAttribute.class);
	}

	@Override
	public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
		if (!context.isValid()) {
			return;
		}

		XmlTag xmlTag;

		if (context instanceof XmlTag) {
			xmlTag = (XmlTag)context;
		}
		else {
			xmlTag = ((XmlAttribute)context).getParent();
		}

		String namespace = xmlTag.getNamespace();
		String localName = xmlTag.getLocalName();

		if (_taglibAttributes.containsKey(namespace)) {
			Collection<SimpleImmutableEntry<String, String>> attributes = _taglibAttributes.get(namespace);

			Stream<SimpleImmutableEntry<String, String>> attributesStream = attributes.stream();

			if (context instanceof XmlTag) {
				attributesStream.filter(
					attribute -> attribute.getKey().equals(localName)
				).filter(
					attribute -> "".equals(attribute.getValue())
				).forEach(
					attribute -> _injectIntoBody(registrar, (XmlTag)context)
				);
			}
			else {
				XmlAttribute xmlAttribute = (XmlAttribute)context;

				String attributeName = xmlAttribute.getLocalName();

				attributesStream.filter(
					attribute -> attribute.getKey().equals(localName)
				).filter(
					attribute -> attribute.getValue().equals(attributeName)
				).forEach(
					attribute -> _injectIntoAttribute(registrar, xmlAttribute)
				);
			}
		}
	}

	private void _injectIntoAttribute(MultiHostRegistrar registrar, XmlAttribute xmlAttribute) {
		XmlAttributeValue xmlAttributeValue = xmlAttribute.getValueElement();

		if (xmlAttributeValue != null) {
			PsiElement[] children = xmlAttributeValue.getChildren();

			boolean needToInject = Stream.of(
				children
			).filter(
				XmlToken.class::isInstance
			).map(
				xmlToken -> (XmlToken)xmlToken
			).map(
				XmlToken::getTokenType
			).anyMatch(
				XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN::equals
			);

			if (needToInject) {
				registrar.startInjecting(JavascriptLanguage.INSTANCE);
				registrar.addPlace(
					null, null, (PsiLanguageInjectionHost)xmlAttributeValue, xmlAttribute.getValueTextRange());
				registrar.doneInjecting();
			}
		}
	}

	private void _injectIntoBody(MultiHostRegistrar registrar, XmlTag xmlTag) {
		PsiElement[] children = xmlTag.getChildren();

		boolean needToInject = Stream.of(
			children
		).anyMatch(
			XmlText.class::isInstance
		);

		if (needToInject) {
			registrar.startInjecting(JavascriptLanguage.INSTANCE);

			Stream.of(
				children
			).filter(
				XmlText.class::isInstance
			).forEach(
				child -> registrar.addPlace(
					null, null, (PsiLanguageInjectionHost)child, new TextRange(0, child.getTextLength()))
			);

			registrar.doneInjecting();
		}
	}

	private static Map<String, Collection<SimpleImmutableEntry<String, String>>> _taglibAttributes = new HashMap<>();

	static {
		_taglibAttributes.put(
			LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI,
			Arrays.asList(
				new SimpleImmutableEntry<>("a", "onClick"), new SimpleImmutableEntry<>("button", "onClick"),
				new SimpleImmutableEntry<>("form", "onSubmit"), new SimpleImmutableEntry<>("input", "onChange"),
				new SimpleImmutableEntry<>("input", "onClick"), new SimpleImmutableEntry<>("script", ""),
				new SimpleImmutableEntry<>("select", "onChange"), new SimpleImmutableEntry<>("select", "onClick"),
				new SimpleImmutableEntry<>("validator", "")));

		_taglibAttributes.put(
			LiferayTaglibs.TAGLIB_URI_LIFERAY_FRONTEND,
			Arrays.asList(
				new SimpleImmutableEntry<>("edit-form", "onSubmit"),
				new SimpleImmutableEntry<>("icon-vertical-card", "onClick"),
				new SimpleImmutableEntry<>("vertical-card", "onClick")));

		_taglibAttributes.put(
			LiferayTaglibs.TAGLIB_URI_LIFERAY_UI,
			Arrays.asList(
				new SimpleImmutableEntry<>("icon", "onClick"), new SimpleImmutableEntry<>("input-checkbox", "onClick"),
				new SimpleImmutableEntry<>("input-move-boxes", "leftOnChange"),
				new SimpleImmutableEntry<>("input-move-boxes", "rightOnChange"),
				new SimpleImmutableEntry<>("page-iterator", "jsCall"),
				new SimpleImmutableEntry<>("quick-access-entry", "onClick"),
				new SimpleImmutableEntry<>("tabs", "onClick")));

		_taglibAttributes.put(
			LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI_OLD, _taglibAttributes.get(LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI));
	}

}