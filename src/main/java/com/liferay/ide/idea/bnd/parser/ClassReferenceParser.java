/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.parser;

import com.intellij.codeInsight.daemon.JavaErrorBundle;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
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
import com.liferay.ide.idea.util.LiferayAnnotationUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.ManifestBundle;

/**
 * @author Dominik Marks
 */
public class ClassReferenceParser extends BndHeaderParser {

	public static final ClassReferenceParser INSTANCE = new ClassReferenceParser();

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
			LiferayAnnotationUtil.createAnnotation(
				holder, HighlightSeverity.ERROR, ManifestBundle.message("header.reference.invalid"),
				valuePart.getHighlightingRange());

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
			LiferayAnnotationUtil.createAnnotation(
				holder, HighlightSeverity.ERROR, JavaErrorBundle.message("error.cannot.resolve.class", className),
				ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);

			return true;
		}

		return checkClass(valuePart, psiClass, holder);
	}

	@NotNull
	@Override
	public PsiReference[] getReferences(@NotNull BndHeaderValuePart bndHeaderValuePart) {
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

		return false;
	}

}