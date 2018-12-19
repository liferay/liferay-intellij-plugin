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

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;

import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibModelContextJavaBeanReferenceProvider extends AbstractLiferayTaglibJavaBeanReferenceProvider {

	@Nullable
	@Override
	protected String getClassName(PsiElement element) {
		XmlTag xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag.class);

		if (xmlTag != null) {
			String modelAttributeValue = xmlTag.getAttributeValue("model");

			if (modelAttributeValue != null) {
				return modelAttributeValue;
			}

			XmlTag modelContextTag = (XmlTag)_getPrevSiblingOrParent(
				element, LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "model-context");

			if (modelContextTag != null) {
				return modelContextTag.getAttributeValue("model");
			}
		}

		return null;
	}

	private static PsiElement _getPrevSiblingOrParent(
		PsiElement element, String classNameElementNamespace, String classNameElementLocalName) {

		PsiElement sibling = element.getPrevSibling();

		while (sibling != null) {
			if (sibling instanceof XmlTag) {
				XmlTag xmlTag = (XmlTag)sibling;

				String namespace = xmlTag.getNamespace();
				String localName = xmlTag.getLocalName();

				if (classNameElementNamespace.equals(namespace)) {
					if (classNameElementLocalName.equals(localName)) {
						return sibling;
					}
				}
			}

			sibling = sibling.getPrevSibling();
		}

		PsiElement parent = element.getParent();

		if (parent != null) {
			return _getPrevSiblingOrParent(parent, classNameElementNamespace, classNameElementLocalName);
		}

		return null;
	}

}