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

package com.liferay.ide.idea.language.service;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTagChild;
import com.intellij.psi.xml.XmlText;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLUtil {

	public static boolean isColumnNameAttribute(@NotNull XmlAttributeValue xmlAttributeValue) {
		return Stream.of(
			xmlAttributeValue
		).map(
			XmlAttributeValue::getParent
		).filter(
			parent -> parent instanceof XmlAttribute
		).map(
			xmlAttribute -> (XmlAttribute)xmlAttribute
		).filter(
			xmlAttribute -> Objects.equals("name", xmlAttribute.getLocalName())
		).map(
			XmlAttribute::getParent
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals("column", parentTag.getLocalName())
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).filter(
			grandParentTag -> Objects.equals("entity", grandParentTag.getLocalName())
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals("service-builder", grandParentTag.getLocalName())
		);
	}

	public static boolean isColumnPrimaryAttribute(@NotNull XmlAttributeValue xmlAttributeValue) {
		return Stream.of(
			xmlAttributeValue
		).map(
			XmlAttributeValue::getParent
		).filter(
			parent -> parent instanceof XmlAttribute
		).map(
			xmlAttribute -> (XmlAttribute)xmlAttribute
		).filter(
			xmlAttribute -> Objects.equals("primary", xmlAttribute.getLocalName())
		).map(
			XmlAttribute::getParent
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals("column", parentTag.getLocalName())
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).filter(
			grandParentTag -> Objects.equals("entity", grandParentTag.getLocalName())
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals("service-builder", grandParentTag.getLocalName())
		);
	}

	public static boolean isEntityNameAttribute(@NotNull XmlAttributeValue xmlAttributeValue) {
		return Stream.of(
			xmlAttributeValue
		).map(
			XmlAttributeValue::getParent
		).filter(
			parent -> parent instanceof XmlAttribute
		).map(
			xmlAttribute -> (XmlAttribute)xmlAttribute
		).filter(
			xmlAttribute -> Objects.equals("name", xmlAttribute.getLocalName())
		).map(
			XmlAttribute::getParent
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals("entity", parentTag.getLocalName())
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals("service-builder", grandParentTag.getLocalName())
		);
	}

	public static boolean isEntityUuidAttribute(@NotNull XmlAttributeValue xmlAttributeValue) {
		return Stream.of(
			xmlAttributeValue
		).map(
			XmlAttributeValue::getParent
		).filter(
			parent -> parent instanceof XmlAttribute
		).map(
			xmlAttribute -> (XmlAttribute)xmlAttribute
		).filter(
			xmlAttribute -> Objects.equals("uuid", xmlAttribute.getLocalName())
		).map(
			XmlAttribute::getParent
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals("entity", parentTag.getLocalName())
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals("service-builder", grandParentTag.getLocalName())
		);
	}

	public static boolean isExceptionTag(@NotNull XmlText xmlText) {
		return Stream.of(
			xmlText
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals("exception", parentTag.getLocalName())
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).filter(
			grandParentTag -> Objects.equals("exceptions", grandParentTag.getLocalName())
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals("service-builder", grandParentTag.getLocalName())
		);
	}

	public static boolean isNamespaceTag(@NotNull XmlText xmlText) {
		return Stream.of(
			xmlText
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals("namespace", parentTag.getLocalName())
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals("service-builder", grandParentTag.getLocalName())
		);
	}

	protected static boolean isFinderNameAttribute(@NotNull XmlAttributeValue xmlAttributeValue) {
		return Stream.of(
			xmlAttributeValue
		).map(
			XmlAttributeValue::getParent
		).filter(
			parent -> parent instanceof XmlAttribute
		).map(
			xmlAttribute -> (XmlAttribute)xmlAttribute
		).filter(
			xmlAttribute -> Objects.equals("name", xmlAttribute.getLocalName())
		).map(
			XmlAttribute::getParent
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals("finder", parentTag.getLocalName())
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).filter(
			grandParentTag -> Objects.equals("entity", grandParentTag.getLocalName())
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals("service-builder", grandParentTag.getLocalName())
		);
	}

}