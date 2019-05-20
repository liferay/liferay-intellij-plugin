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

package com.liferay.ide.idea.bnd.completion;

import static com.intellij.patterns.PlatformPatterns.psiElement;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;

import com.liferay.ide.idea.bnd.psi.Directive;

import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;

import org.osgi.framework.Constants;

/**
 * @author Charles Wu
 */
public class OsgiManifestCompletionContributor extends CompletionContributor {

	public OsgiManifestCompletionContributor() {
		extend(
			CompletionType.BASIC, _header(Constants.EXPORT_PACKAGE),
			new HeaderParametersProvider(Constants.VERSION_ATTRIBUTE, Constants.USES_DIRECTIVE + ':'));

		extend(
			CompletionType.BASIC, _header(Constants.IMPORT_PACKAGE),
			new HeaderParametersProvider(Constants.VERSION_ATTRIBUTE, Constants.RESOLUTION_DIRECTIVE + ':'));

		extend(
			CompletionType.BASIC, _directive(Constants.RESOLUTION_DIRECTIVE),
			new SimpleProvider(Constants.RESOLUTION_MANDATORY, Constants.RESOLUTION_OPTIONAL));
	}

	private static ElementPattern<PsiElement> _directive(String name) {
		PsiElementPattern.Capture<PsiElement> element = psiElement(ManifestTokenType.HEADER_VALUE_PART);

		PsiElementPattern.Capture<Directive> directiveElement = psiElement(Directive.class);

		return element.withSuperParent(2, directiveElement.withName(name));
	}

	private static ElementPattern<PsiElement> _header(String name) {
		PsiElementPattern.Capture<PsiElement> psiElementCapture = psiElement(ManifestTokenType.HEADER_VALUE_PART);

		psiElementCapture.afterLeaf(";");

		PsiElementPattern.Capture<Header> headerElement = psiElement(Header.class);

		return psiElementCapture.withSuperParent(3, headerElement.withName(name));
	}

}