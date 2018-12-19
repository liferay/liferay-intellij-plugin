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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibModelContextJavaBeanReferenceContributor extends AbstractLiferayTaglibReferenceContributor {

	@Override
	protected String[] getAttributeNames() {
		return new String[] {"name", "field"};
	}

	@Override
	protected PsiReferenceProvider getReferenceProvider() {
		return new LiferayTaglibModelContextJavaBeanReferenceProvider();
	}

	@Override
	protected boolean isSuitableAttribute(XmlAttribute xmlAttribute) {
		XmlTag xmlTag = xmlAttribute.getParent();

		if (xmlTag != null) {
			String namespace = xmlTag.getNamespace();
			String localName = xmlTag.getLocalName();
			String attributeName = xmlAttribute.getLocalName();

			if (_taglibAttributes.containsKey(namespace)) {
				Collection<AbstractMap.SimpleImmutableEntry<String, String>> entries = _taglibAttributes.get(namespace);

				Stream<AbstractMap.SimpleImmutableEntry<String, String>> entriesStream = entries.stream();

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

	private static Map<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>> _taglibAttributes =
		new HashMap<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>>() {
			{
				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI,
					Arrays.asList(
						new SimpleImmutableEntry<>("input", "field"), new SimpleImmutableEntry<>("input", "name"),
						new SimpleImmutableEntry<>("select", "field"), new SimpleImmutableEntry<>("select", "name")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_UI,
					Arrays.asList(new SimpleImmutableEntry<>("input-field", "field")));
			}
		};

}