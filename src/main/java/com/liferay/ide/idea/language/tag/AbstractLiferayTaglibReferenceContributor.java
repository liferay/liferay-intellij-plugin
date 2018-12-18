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
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.xml.util.XmlUtil;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public abstract class AbstractLiferayTaglibReferenceContributor extends PsiReferenceContributor {

	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
		XmlUtil.registerXmlAttributeValueReferenceProvider(
			registrar, getAttributeNames(), new LiferayTaglibFilter(), false, getReferenceProvider());
	}

	protected abstract String[] getAttributeNames();

	protected abstract PsiReferenceProvider getReferenceProvider();

	protected abstract boolean isSuitableAttribute(XmlAttribute xmlAttribute);

	private class LiferayTaglibFilter implements ElementFilter {

		public boolean isAcceptable(Object element, PsiElement context) {
			PsiElement psiElement = (PsiElement)element;

			PsiElement parent = psiElement.getParent();

			if (parent instanceof XmlAttribute) {
				XmlAttribute xmlAttribute = (XmlAttribute)parent;

				return AbstractLiferayTaglibReferenceContributor.this.isSuitableAttribute(xmlAttribute);
			}

			return false;
		}

		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}

	}

}