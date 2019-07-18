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

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibSearchContainerJavaBeanReferenceProvider
	extends AbstractLiferayTaglibJavaBeanReferenceProvider {

	@Nullable
	@Override
	protected String getClassName(PsiElement element) {
		PsiElement classNamePsiElement = PsiTreeUtil.findFirstParent(
			element,
			psiElement -> {
				if (psiElement instanceof XmlTag) {
					XmlTag xmlTag = (XmlTag)psiElement;

					if (LiferayTaglibs.TAGLIB_URI_LIFERAY_UI.equals(xmlTag.getNamespace())) {
						return Objects.equals("search-container-row", xmlTag.getLocalName());
					}
				}

				return false;
			});

		if (classNamePsiElement != null) {
			XmlTag xmlTag = (XmlTag)classNamePsiElement;

			return xmlTag.getAttributeValue("className");
		}

		return null;
	}

}