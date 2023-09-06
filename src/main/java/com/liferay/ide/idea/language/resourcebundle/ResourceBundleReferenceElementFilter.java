/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.resourcebundle;

import com.intellij.psi.JavaResolveResult;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class ResourceBundleReferenceElementFilter implements ElementFilter {

	@Override
	public boolean isAcceptable(Object element, @Nullable PsiElement context) {
		if (element instanceof PsiLiteralExpression) {
			PsiLiteralExpression literalExpression = (PsiLiteralExpression)element;

			PsiExpressionList expressionList = PsiTreeUtil.getParentOfType(literalExpression, PsiExpressionList.class);

			if (expressionList != null) {
				int index = ArrayUtil.indexOf(expressionList.getExpressions(), literalExpression);

				PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(
					literalExpression, PsiMethodCallExpression.class);

				if (methodCallExpression != null) {
					PsiReferenceExpression methodExpression = methodCallExpression.getMethodExpression();

					JavaResolveResult resolveResult = methodExpression.advancedResolve(false);

					PsiMethod method = (PsiMethod)resolveResult.getElement();

					if (method != null) {
						for (Map.Entry<String, AbstractMap.SimpleEntry<String, Integer>> entry :
								_resourceBundleMethods) {

							AbstractMap.SimpleEntry<String, Integer> simpleEntry = entry.getValue();

							String key = simpleEntry.getKey();

							if (key.equals(method.getName())) {
								Integer value = simpleEntry.getValue();

								if (value.equals(index)) {
									PsiClass psiClass = (PsiClass)method.getParent();

									if (psiClass != null) {
										String entryKey = entry.getKey();

										if (entryKey.equals(psiClass.getQualifiedName())) {
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean isClassAcceptable(Class hintClass) {
		return true;
	}

	private static final Collection<AbstractMap.SimpleEntry<String, AbstractMap.SimpleEntry<String, Integer>>>
		_resourceBundleMethods = new ArrayList<>() {
			{
				add(
					new AbstractMap.SimpleEntry<>(
						"java.util.ResourceBundle", new AbstractMap.SimpleEntry<>("getBundle", 0)));
				add(
					new AbstractMap.SimpleEntry<>(
						"com.liferay.portal.kernel.util.ResourceBundleUtil",
						new AbstractMap.SimpleEntry<>("getBundle", 0)));
				add(
					new AbstractMap.SimpleEntry<>(
						"com.liferay.portal.kernel.util.ResourceBundleUtil",
						new AbstractMap.SimpleEntry<>("getLocalizationMap", 0)));
			}
		};

}