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

import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibSearchContainerJavaBeanReferenceContributor
	extends AbstractLiferayTaglibReferenceContributor {

	@Override
	protected String[] getAttributeNames() {
		return new String[] {"property", "name", "orderableProperty", "keyProperty", "rowIdProperty"};
	}

	@Override
	protected PsiReferenceProvider getReferenceProvider() {
		return new LiferayTaglibSearchContainerJavaBeanReferenceProvider();
	}

	@Override
	protected boolean isSuitableAttribute(XmlAttribute xmlAttribute) {
		XmlTag xmlTag = xmlAttribute.getParent();

		if (xmlTag != null) {
			String namespace = xmlTag.getNamespace();
			String localName = xmlTag.getLocalName();
			String attributeName = xmlAttribute.getLocalName();

			if (LiferayTaglibs.TAGLIB_URI_LIFERAY_UI.equals(namespace)) {
				Stream<AbstractMap.SimpleImmutableEntry<String, String>> entriesStream = _taglibAttributes.stream();

				return entriesStream.anyMatch(
					entry -> {
						String key = entry.getKey();
						String value = entry.getValue();

						if (key.equals(localName) && value.equals(attributeName)) {
							return true;
						}

						return false;
					});
			}
		}

		return false;
	}

	private static Collection<AbstractMap.SimpleImmutableEntry<String, String>> _taglibAttributes = Arrays.asList(
		new AbstractMap.SimpleImmutableEntry<>("search-container-column-text", "property"),
		new AbstractMap.SimpleImmutableEntry<>("search-container-column-text", "name"),
		new AbstractMap.SimpleImmutableEntry<>("search-container-column-text", "orderableProperty"),
		new AbstractMap.SimpleImmutableEntry<>("search-container-column-date", "property"),
		new AbstractMap.SimpleImmutableEntry<>("search-container-column-status", "property"),
		new AbstractMap.SimpleImmutableEntry<>("search-container-column-user", "property"),
		new AbstractMap.SimpleImmutableEntry<>("search-container-row", "keyProperty"),
		new AbstractMap.SimpleImmutableEntry<>("search-container-row", "rowIdProperty"));

}