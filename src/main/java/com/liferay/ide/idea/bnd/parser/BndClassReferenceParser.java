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

package com.liferay.ide.idea.bnd.parser;

import com.intellij.codeInsight.daemon.JavaErrorMessages;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;

import com.liferay.ide.idea.bnd.psi.BndHeader;
import com.liferay.ide.idea.bnd.psi.BndHeaderValue;
import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;
import com.liferay.ide.idea.bnd.psi.Clause;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.ManifestBundle;

/**
 * @author Dominik Marks
 */
public class BndClassReferenceParser extends BndHeaderParser {

	@Override
	public boolean annotate(@NotNull BndHeader bndHeader, @NotNull AnnotationHolder holder) {
		BndHeaderValue value = bndHeader.getBndHeaderValue();

		BndHeaderValuePart valuePart = null;

		if (value instanceof BndHeaderValuePart) {
			valuePart = (BndHeaderValuePart)value;
		}
		else if (value instanceof Clause) {
			Clause clause = (Clause)value;

			valuePart = clause.getValue();
		}

		if (valuePart == null) {
			return false;
		}

		String className = valuePart.getUnwrappedText();

		if (StringUtil.isEmptyOrSpaces(className)) {
			//TODO create a test!
			holder.createErrorAnnotation(
				valuePart.getHighlightingRange(), ManifestBundle.message("header.reference.invalid"));

			return true;
		}

		Project project = bndHeader.getProject();
		Module module = ModuleUtilCore.findModuleForPsiElement(bndHeader);

		GlobalSearchScope globalSearchScope;

		if (module == null) {
			globalSearchScope = ProjectScope.getAllScope(project);
		}
		else {
			globalSearchScope = module.getModuleWithDependenciesAndLibrariesScope(false);
		}

		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);

		PsiClass psiClass = javaPsiFacade.findClass(className, globalSearchScope);

		if (psiClass == null) {
			//TODO create a test!
			String message = JavaErrorMessages.message("error.cannot.resolve.class", className);

			Annotation annotation = holder.createErrorAnnotation(valuePart.getHighlightingRange(), message);

			annotation.setHighlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);

			return true;
		}

		return checkClass(valuePart, psiClass, holder);
	}

	@NotNull
	@Override
	public PsiReference[] getReferences(@NotNull BndHeaderValuePart bndHeaderValuePart) {
		//TODO add test to resolve reference
		Module module = ModuleUtilCore.findModuleForPsiElement(bndHeaderValuePart);

		JavaClassReferenceProvider javaClassReferenceProvider;

		if (module != null) {
			javaClassReferenceProvider = new JavaClassReferenceProvider() {

				@Override
				public GlobalSearchScope getScope(Project project) {
					return GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
				}

			};
		}
		else {
			javaClassReferenceProvider = new JavaClassReferenceProvider();
		}

		return javaClassReferenceProvider.getReferencesByElement(bndHeaderValuePart);
	}

	protected boolean checkClass(
		@NotNull BndHeaderValuePart bndHeaderValuePart, @NotNull PsiClass psiClass,
		@NotNull AnnotationHolder annotationHolder) {

		//TODO implement

		return false;
	}

}