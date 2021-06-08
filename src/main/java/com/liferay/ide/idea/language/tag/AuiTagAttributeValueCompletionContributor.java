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

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * @author Terry Jia
 */
public class AuiTagAttributeValueCompletionContributor extends CompletionContributor {

	public AuiTagAttributeValueCompletionContributor() {
		for (String[] attribute : _parseBooleanAttributes()) {
			PsiElementPattern.Capture<PsiElement> capture = PlatformPatterns.psiElement();

			extend(
				CompletionType.BASIC, capture.with(new TagPatternCondition(attribute[0], attribute[1], attribute[2])),
				new BooleanCompletionProvider());
		}

		for (String[] attribute : _getLexiconAttributes()) {
			PsiElementPattern.Capture<PsiElement> capture = PlatformPatterns.psiElement();

			extend(
				CompletionType.BASIC, capture.with(new TagPatternCondition(attribute[0], attribute[1], attribute[2])),
				new StringCompletionProvider(new String[] {"lexicon"}));
		}

		for (String[] attribute : _getLeftRightAttributes()) {
			PsiElementPattern.Capture<PsiElement> capture = PlatformPatterns.psiElement();

			extend(
				CompletionType.BASIC, capture.with(new TagPatternCondition(attribute[0], attribute[1], attribute[2])),
				new StringCompletionProvider(new String[] {"left", "right"}));
		}

		for (String[] attribute : _getTargetAttributes()) {
			PsiElementPattern.Capture<PsiElement> capture = PlatformPatterns.psiElement();

			extend(
				CompletionType.BASIC, capture.with(new TagPatternCondition(attribute[0], attribute[1], attribute[2])),
				new StringCompletionProvider(new String[] {"blank", "self", "parent", "top", "_blank", "_new"}));
		}

		PsiElementPattern.Capture<PsiElement> buttonCapture = PlatformPatterns.psiElement();

		extend(
			CompletionType.BASIC,
			buttonCapture.with(new TagPatternCondition(LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "button", "type")),
			new StringCompletionProvider(new String[] {"button", "submit", "cancel", "reset"}));

		PsiElementPattern.Capture<PsiElement> formCapture = PlatformPatterns.psiElement();

		extend(
			CompletionType.BASIC,
			formCapture.with(new TagPatternCondition(LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "form", "method")),
			new StringCompletionProvider(new String[] {"get", "post"}));

		PsiElementPattern.Capture<PsiElement> inputCapture = PlatformPatterns.psiElement();

		extend(
			CompletionType.BASIC,
			inputCapture.with(new TagPatternCondition(LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "input", "type")),
			new StringCompletionProvider(
				new String[] {
					"text", "hidden", "assetCategories", "assetTags", "textarea", "timeZone", "password", "checkbox",
					"radio", "submit", "button", "color", "email", "number", "range", "resource", "url", "editor",
					"toggle-card", "toggle-switch", "image"
				}));

		PsiElementPattern.Capture<PsiElement> validatorCapture = PlatformPatterns.psiElement();

		extend(
			CompletionType.BASIC,
			validatorCapture.with(new TagPatternCondition(LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "validator", "name")),
			new StringCompletionProvider(
				new String[] {
					"acceptFiles", "alpha", "alphanum", "custom", "date", "digits", "email", "equalTo", "iri", "max",
					"maxLength", "min", "minLength", "number", "range", "rangeLength", "required", "url"
				}));
	}

	private List<String[]> _getLeftRightAttributes() {
		List<String[]> leftRightAttributes = new ArrayList<>();

		leftRightAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "field-wrapper", "inlineLabel"});
		leftRightAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "form", "inlineLabel"});
		leftRightAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "input", "inlineLabel"});
		leftRightAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "select", "inlineLabel"});
		leftRightAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "button", "iconAlign"});

		return leftRightAttributes;
	}

	private List<String[]> _getLexiconAttributes() {
		List<String[]> lexiconAttributes = new ArrayList<>();

		lexiconAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "fieldset", "markupView"});
		lexiconAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "fieldset-group", "markupView"});
		lexiconAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "icon", "markupView"});
		lexiconAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "nav-bar", "markupView"});
		lexiconAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "workflow-status", "markupView"});

		return lexiconAttributes;
	}

	private List<String[]> _getTargetAttributes() {
		List<String[]> targetAttributes = new ArrayList<>();

		targetAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "a", "target"});
		targetAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "icon", "target"});
		targetAttributes.add(new String[] {LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "nav-item", "target"});

		return targetAttributes;
	}

	private List<String[]> _parseBooleanAttributes() {
		List<String[]> booleanAttributes = new ArrayList<>();

		SAXReader saxReader = new SAXReader();

		ClassLoader classLoader = AuiTagAttributeValueCompletionContributor.class.getClassLoader();

		URL[] urls = {
			classLoader.getResource("definitions/tld/liferay-aui.tld"),
			classLoader.getResource("definitions/tld/liferay-ui.tld")
		};

		for (URL url : urls) {
			try {
				Document document = saxReader.read(url);

				Node uriNode = document.selectSingleNode("/*[name()='taglib']/*[name()='uri']");

				String uri = uriNode.getText();

				List<Node> typeNodes = document.selectNodes(
					"/*[name()='taglib']/*[name()='tag']/*[name()='attribute']/*[name()='type']");

				for (Node typeNode : typeNodes) {
					String type = typeNode.getText();

					if (type.equals("boolean")) {
						Node attributeName = typeNode.selectSingleNode("../*[name()='name']");

						Node tagName = attributeName.selectSingleNode("../../*[name()='name']");

						booleanAttributes.add(new String[] {uri, tagName.getText(), attributeName.getText()});
					}
				}
			}
			catch (DocumentException documentException) {
			}
		}

		return booleanAttributes;
	}

}