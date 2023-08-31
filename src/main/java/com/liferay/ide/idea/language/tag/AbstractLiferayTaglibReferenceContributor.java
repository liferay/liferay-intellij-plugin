/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.tag;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.XmlUtil;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public abstract class AbstractLiferayTaglibReferenceContributor extends PsiReferenceContributor {

	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
		XmlUtil.registerXmlAttributeValueReferenceProvider(
			registrar, getAttributeNames(), new LiferayTaglibFilter(), false, getPsiReferenceProvider());
	}

	protected abstract String[] getAttributeNames();

	protected abstract PsiReferenceProvider getPsiReferenceProvider();

	protected abstract Map<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>>
		getTaglibAttributesMap();

	protected boolean isSuitableXmlAttribute(XmlAttribute xmlAttribute) {
		XmlTag xmlTag = xmlAttribute.getParent();

		if (xmlTag != null) {
			String namespace = xmlTag.getNamespace();

			Map<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>> taglibAttributesMap =
				getTaglibAttributesMap();

			if (taglibAttributesMap.containsKey(namespace)) {
				Collection<AbstractMap.SimpleImmutableEntry<String, String>> entries = taglibAttributesMap.get(
					namespace);

				Stream<AbstractMap.SimpleImmutableEntry<String, String>> entriesStream = entries.stream();

				return entriesStream.anyMatch(
					entry -> {
						String key = entry.getKey();
						String value = entry.getValue();

						if (key.equals(xmlTag.getLocalName()) && value.equals(xmlAttribute.getLocalName())) {
							return true;
						}

						return false;
					});
			}
		}

		return false;
	}

	private class LiferayTaglibFilter implements ElementFilter {

		public boolean isAcceptable(Object element, PsiElement psiElementContext) {
			PsiElement psiElement = (PsiElement)element;

			PsiElement psiElementParent = psiElement.getParent();

			if (psiElementParent instanceof XmlAttribute) {
				XmlAttribute xmlAttribute = (XmlAttribute)psiElementParent;

				return AbstractLiferayTaglibReferenceContributor.this.isSuitableXmlAttribute(xmlAttribute);
			}

			return false;
		}

		@SuppressWarnings("rawtypes")
		public boolean isClassAcceptable(Class hintClass) {
			return true;
		}

	}

}