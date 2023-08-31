/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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
			xmlAttribute -> Objects.equals(xmlAttribute.getLocalName(), "name")
		).map(
			XmlAttribute::getParent
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals(parentTag.getLocalName(), "column")
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).filter(
			grandParentTag -> Objects.equals(grandParentTag.getLocalName(), "entity")
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals(grandParentTag.getLocalName(), "service-builder")
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
			xmlAttribute -> Objects.equals(xmlAttribute.getLocalName(), "primary")
		).map(
			XmlAttribute::getParent
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals(parentTag.getLocalName(), "column")
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).filter(
			grandParentTag -> Objects.equals(grandParentTag.getLocalName(), "entity")
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals(grandParentTag.getLocalName(), "service-builder")
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
			xmlAttribute -> Objects.equals(xmlAttribute.getLocalName(), "name")
		).map(
			XmlAttribute::getParent
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals(parentTag.getLocalName(), "entity")
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals(grandParentTag.getLocalName(), "service-builder")
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
			xmlAttribute -> Objects.equals(xmlAttribute.getLocalName(), "uuid")
		).map(
			XmlAttribute::getParent
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals(parentTag.getLocalName(), "entity")
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals(grandParentTag.getLocalName(), "service-builder")
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
			parentTag -> Objects.equals(parentTag.getLocalName(), "exception")
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).filter(
			grandParentTag -> Objects.equals(grandParentTag.getLocalName(), "exceptions")
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals(grandParentTag.getLocalName(), "service-builder")
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
			parentTag -> Objects.equals(parentTag.getLocalName(), "namespace")
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals(grandParentTag.getLocalName(), "service-builder")
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
			xmlAttribute -> Objects.equals(xmlAttribute.getLocalName(), "name")
		).map(
			XmlAttribute::getParent
		).filter(
			Objects::nonNull
		).filter(
			parentTag -> Objects.equals(parentTag.getLocalName(), "finder")
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).filter(
			grandParentTag -> Objects.equals(grandParentTag.getLocalName(), "entity")
		).map(
			XmlTagChild::getParentTag
		).filter(
			Objects::nonNull
		).anyMatch(
			grandParentTag -> Objects.equals(grandParentTag.getLocalName(), "service-builder")
		);
	}

}