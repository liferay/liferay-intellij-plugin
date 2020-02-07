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

package com.liferay.ide.idea.bnd.psi.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.osgi.framework.BundleActivator;

/**
 * @author Dominik Marks
 */
public class BndPsiUtil {

	@Nullable
	public static PsiClass getBundleActivatorClass(@NotNull PsiElement psiElement) {
		Project project = psiElement.getProject();

		Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);

		GlobalSearchScope globalSearchScope;

		if (module == null) {
			globalSearchScope = ProjectScope.getLibrariesScope(project);
		}
		else {
			globalSearchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
		}

		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);

		return javaPsiFacade.findClass(BundleActivator.class.getName(), globalSearchScope);
	}

}