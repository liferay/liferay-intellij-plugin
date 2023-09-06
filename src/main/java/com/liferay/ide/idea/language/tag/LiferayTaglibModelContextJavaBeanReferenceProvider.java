/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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
	protected String getClassName(PsiElement psiElement) {
		XmlTag xmlTag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);

		if (xmlTag != null) {
			String modelAttributeValue = xmlTag.getAttributeValue("model");

			if (modelAttributeValue != null) {
				return modelAttributeValue;
			}

			XmlTag modelContextXmlTag = (XmlTag)_getPrevSiblingOrParent(
				psiElement, LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI, "model-context");

			if (modelContextXmlTag != null) {
				return modelContextXmlTag.getAttributeValue("model");
			}
		}

		return null;
	}

	private PsiElement _getPrevSiblingOrParent(
		PsiElement psiElement, String classNameElementNamespace, String classNameElementLocalName) {

		PsiElement siblingPsiElement = psiElement.getPrevSibling();

		while (siblingPsiElement != null) {
			if (siblingPsiElement instanceof XmlTag) {
				XmlTag xmlTag = (XmlTag)siblingPsiElement;

				if (classNameElementNamespace.equals(xmlTag.getNamespace()) &&
					classNameElementLocalName.equals(xmlTag.getLocalName())) {

					return siblingPsiElement;
				}
			}

			siblingPsiElement = siblingPsiElement.getPrevSibling();
		}

		PsiElement parentPsiElement = psiElement.getParent();

		if (parentPsiElement != null) {
			return _getPrevSiblingOrParent(parentPsiElement, classNameElementNamespace, classNameElementLocalName);
		}

		return null;
	}

}