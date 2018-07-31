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

import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;

import org.jetbrains.annotations.NotNull;

/**
 * @author Terry Jia
 */
public class TagPatternCondition extends PatternCondition<PsiElement> {

	public TagPatternCondition(String uri, String name, String attribute) {
		super("pattern");

		_uri = uri;
		_name = name;
		_attribute = attribute;
	}

	@Override
	public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
		XmlAttributeValue attributeValue = PsiTreeUtil.getParentOfType(psiElement, XmlAttributeValue.class);

		if (attributeValue == null) {
			return false;
		}

		XmlAttribute attribute = PsiTreeUtil.getParentOfType(attributeValue, XmlAttribute.class);

		if (attribute == null) {
			return false;
		}

		String attributeName = attribute.getName();

		if (attributeName.equals(_attribute)) {
			XmlTag tag = PsiTreeUtil.getParentOfType(attributeValue, XmlTag.class);

			if (tag != null) {
				String tagName = tag.getLocalName();
				String namespace = tag.getNamespace();

				if (tagName.equals(_name) && _uri.equals(namespace)) {
					return true;
				}
			}
		}

		return false;
	}

	private String _attribute;
	private String _name;
	private String _uri;

}