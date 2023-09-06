/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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
						return Objects.equals(xmlTag.getLocalName(), "search-container-row");
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