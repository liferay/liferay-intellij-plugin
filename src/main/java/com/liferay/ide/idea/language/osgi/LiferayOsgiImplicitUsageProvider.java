/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.osgi;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;

import java.util.Arrays;
import java.util.Collection;

/**
 * Avoid showing "never assigned" warning for fields annotated by Reference or Inject
 *
 * @author Dominik Marks
 */
public class LiferayOsgiImplicitUsageProvider implements ImplicitUsageProvider {

	@Override
	public boolean isImplicitRead(PsiElement psiElement) {
		return false;
	}

	@Override
	public boolean isImplicitUsage(PsiElement psiElement) {
		return isImplicitWrite(psiElement);
	}

	@Override
	public boolean isImplicitWrite(PsiElement psiElement) {
		if (psiElement instanceof PsiModifierListOwner) {
			if (psiElement instanceof PsiMethod) {
				PsiMethod psiMethod = (PsiMethod)psiElement;

				if (psiMethod.isConstructor()) {
					PsiClass containingClass = psiMethod.getContainingClass();

					if ((containingClass != null) &&
						AnnotationUtil.isAnnotated(
							containingClass, "org.osgi.service.component.annotations.Component",
							AnnotationUtil.CHECK_TYPE)) {

						return true;
					}

					return false;
				}
			}

			PsiModifierListOwner psiModifierListOwner = (PsiModifierListOwner)psiElement;

			boolean annotated = AnnotationUtil.isAnnotated(
				psiModifierListOwner, _writeAnnotations, AnnotationUtil.CHECK_TYPE);

			if (annotated) {
				return true;
			}
		}

		return false;
	}

	private static final Collection<String> _writeAnnotations = Arrays.asList(
		"com.liferay.arquillian.containter.remote.enricher.Inject",
		"com.liferay.arquillian.portal.annotation.PortalURL", "com.liferay.portal.kernel.bean.BeanReference",
		"com.liferay.portal.spring.extender.service.ServiceReference", "com.liferay.portal.test.rule.Inject",
		"javax.inject.Inject", "org.jboss.arquillian.core.api.annotation.Inject",
		"org.osgi.service.component.annotations.Activate", "org.osgi.service.component.annotations.Component",
		"org.osgi.service.component.annotations.Deactivate", "org.osgi.service.component.annotations.Reference",
		"org.osgi.service.component.annotations.Modified");

}