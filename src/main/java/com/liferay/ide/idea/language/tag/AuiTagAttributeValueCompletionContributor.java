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
				CompletionType.BASIC,
				capture.with(new TagPatternCondition(attribute[0], attribute[1], attribute[2])),
				new BooleanCompletionProvider());
		}

		for (String[] attribute : _getLexiconAttributes()) {
			PsiElementPattern.Capture<PsiElement> capture = PlatformPatterns.psiElement();

			extend(
				CompletionType.BASIC,
				capture.with(new TagPatternCondition(attribute[0], attribute[1], attribute[2])),
				new StringCompletionProvider(new String[] {"lexicon"}));
		}

		for (String[] attribute : _getLeftRightAttributes()) {
			PsiElementPattern.Capture<PsiElement> capture = PlatformPatterns.psiElement();

			extend(
				CompletionType.BASIC,
				capture.with(new TagPatternCondition(attribute[0], attribute[1], attribute[2])),
				new StringCompletionProvider(new String[] {"left", "right"}));
		}

		for (String[] attribute : _getTargetAttributes()) {
			PsiElementPattern.Capture<PsiElement> capture = PlatformPatterns.psiElement();

			extend(
				CompletionType.BASIC,
				capture.with(new TagPatternCondition(attribute[0], attribute[1], attribute[2])),
				new StringCompletionProvider(new String[] {"blank", "self", "parent", "top", "_blank", "_new"}));
		}

		PsiElementPattern.Capture<PsiElement> buttonCapture = PlatformPatterns.psiElement();

		extend(
			CompletionType.BASIC, buttonCapture.with(new TagPatternCondition("aui", "button", "type")),
			new StringCompletionProvider(new String[] {"button", "submit", "cancel", "reset"}));

		PsiElementPattern.Capture<PsiElement> formCapture = PlatformPatterns.psiElement();

		extend(
			CompletionType.BASIC, formCapture.with(new TagPatternCondition("aui", "form", "method")),
			new StringCompletionProvider(new String[] {"get", "post"}));

		PsiElementPattern.Capture<PsiElement> inputCapture = PlatformPatterns.psiElement();

		extend(
			CompletionType.BASIC, inputCapture.with(new TagPatternCondition("aui", "input", "type")),
			new StringCompletionProvider(
				new String[] {
					"text", "hidden", "assetCategories", "assetTags", "textarea", "timeZone", "password", "checkbox",
					"radio", "submit", "button", "color", "email", "number", "range", "resource", "url", "editor",
					"toggle-card", "toggle-switch", "image"
				}));

		PsiElementPattern.Capture<PsiElement> validatorCapture = PlatformPatterns.psiElement();

		extend(
			CompletionType.BASIC,
			validatorCapture.with(new TagPatternCondition("aui", "validator", "name")),
			new StringCompletionProvider(
				new String[] {
					"custom", "acceptFiles", "alpha", "alphanum", "date", "digits", "email", "equalTo", "iri", "max",
					"maxLength", "min", "minLength", "number", "range", "rangeLength", "required", "url"
				}));
	}

	private static List<String[]> _getLeftRightAttributes() {
		List<String[]> leftRightAttributes = new ArrayList<>();

		leftRightAttributes.add(new String[] {"aui", "field-wrapper", "inlineLabel"});
		leftRightAttributes.add(new String[] {"aui", "form", "inlineLabel"});
		leftRightAttributes.add(new String[] {"aui", "input", "inlineLabel"});
		leftRightAttributes.add(new String[] {"aui", "select", "inlineLabel"});
		leftRightAttributes.add(new String[] {"aui", "button", "iconAlign"});

		return leftRightAttributes;
	}

	private static List<String[]> _getLexiconAttributes() {
		List<String[]> lexiconAttributes = new ArrayList<>();

		lexiconAttributes.add(new String[] {"aui", "fieldset", "markupView"});
		lexiconAttributes.add(new String[] {"aui", "fieldset-group", "markupView"});
		lexiconAttributes.add(new String[] {"aui", "icon", "markupView"});
		lexiconAttributes.add(new String[] {"aui", "nav-bar", "markupView"});
		lexiconAttributes.add(new String[] {"aui", "workflow-status", "markupView"});

		return lexiconAttributes;
	}

	private static List<String[]> _getTargetAttributes() {
		List<String[]> lexiconAttributes = new ArrayList<>();

		lexiconAttributes.add(new String[] {"aui", "a", "target"});
		lexiconAttributes.add(new String[] {"aui", "icon", "target"});
		lexiconAttributes.add(new String[] {"aui", "nav-item", "target"});

		return lexiconAttributes;
	}

	private static List<String[]> _parseBooleanAttributes() {
		List<String[]> booleanAttributes = new ArrayList<>();

		SAXReader reader = new SAXReader();

		ClassLoader classLoader = AuiTagAttributeValueCompletionContributor.class.getClassLoader();

		URL[] urls = {classLoader.getResource("/tld/liferay-aui.tld"), classLoader.getResource("/tld/liferay-ui.tld")};

		for (URL url : urls) {
			try {
				Document document = reader.read(url);

				Node shortNameNode = document.selectSingleNode("/*[name()='taglib']/*[name()='short-name']");

				String shortName = shortNameNode.getText();

				List<Node> typeNodes = document.selectNodes(
					"/*[name()='taglib']/*[name()='tag']/*[name()='attribute']/*[name()='type']");

				for (Node typeNode : typeNodes) {
					String type = typeNode.getText();

					if (type.equals("boolean")) {
						Node attributeName = typeNode.selectSingleNode("../*[name()='name']");

						Node tagName = attributeName.selectSingleNode("../../*[name()='name']");

						booleanAttributes.add(new String[] {shortName, tagName.getText(), attributeName.getText()});
					}
				}
			}
			catch (DocumentException de) {
				de.printStackTrace();
			}
		}

		return booleanAttributes;
	}

}