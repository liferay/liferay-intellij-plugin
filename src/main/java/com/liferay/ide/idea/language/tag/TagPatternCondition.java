/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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

				if (tagName.equals(_name) && _uri.equals(tag.getNamespace())) {
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