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

package com.liferay.ide.idea.language.osgi;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Avoid showing "never assigned" warning for fields annotated by Reference or Inject
 *
 * @author Dominik Marks
 */
public class LiferayOsgiImplicitUsageProvider implements ImplicitUsageProvider {

	@Override
	public boolean isImplicitRead(PsiElement element) {
		return false;
	}

	@Override
	public boolean isImplicitUsage(PsiElement element) {
		return isImplicitWrite(element);
	}

	@Override
	public boolean isImplicitWrite(PsiElement element) {
		return Stream.of(
			element
		).filter(
			modifierListOwner -> modifierListOwner instanceof PsiModifierListOwner
		).map(
			modifierListOwner -> (PsiModifierListOwner)modifierListOwner
		).anyMatch(
			modifierListOwner -> AnnotationUtil.isAnnotated(
				modifierListOwner, _writeAnnotations, AnnotationUtil.CHECK_TYPE)
		);
	}

	private static final Collection<String> _writeAnnotations = Arrays.asList(
		"org.osgi.service.component.annotations.Reference", "org.osgi.service.component.annotations.Activate",
		"org.osgi.service.component.annotations.Deactivate", "org.osgi.service.component.annotations.Modified",
		"com.liferay.portal.spring.extender.service.ServiceReference", "com.liferay.portal.kernel.bean.BeanReference",
		"com.liferay.arquillian.containter.remote.enricher.Inject",
		"com.liferay.arquillian.portal.annotation.PortalURL", "org.jboss.arquillian.core.api.annotation.Inject");

}