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
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PackageReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiPackageReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.util.containers.ContainerUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.osgi.framework.BundleActivator;

/**
 * @author Dominik Marks
 */
public class BndPsiUtil {

	public static TextRange adjustTextRangeWithoutWhitespaces(TextRange textRange, String text) {
		int end = textRange.getEndOffset();
		int start = textRange.getStartOffset();

		while ((end > start) && Character.isWhitespace(text.charAt(end - 1))) {
			end--;
		}

		while ((start < end) && Character.isWhitespace(text.charAt(start))) {
			start++;
		}

		return new TextRange(start, end);
	}

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

	@NotNull
	public static PsiReference[] getFileReferences(@NotNull PsiElement psiElement) {
		String filePath = psiElement.getText();

		Project project = psiElement.getProject();

		Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);

		if (module != null) {
			PsiManager psiManager = PsiManager.getInstance(project);

			ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

			VirtualFile[] sourceRoots = moduleRootManager.getSourceRoots(false);

			Collection<PsiFileSystemItem> psiDirectories = new ArrayList<>();

			for (VirtualFile sourceRoot : sourceRoots) {
				PsiDirectory psiDirectory = psiManager.findDirectory(sourceRoot);

				if (psiDirectory != null) {
					psiDirectories.add(psiDirectory);
				}
			}

			FileReferenceSet fileReferenceSet = new FileReferenceSet(filePath, psiElement, 0, null, true) {

				@NotNull
				@Override
				public Collection<PsiFileSystemItem> getDefaultContexts() {
					return psiDirectories;
				}

			};

			return fileReferenceSet.getAllReferences();
		}

		return PsiReference.EMPTY_ARRAY;
	}

	@NotNull
	public static PsiReference[] getPackageReferences(@NotNull PsiElement psiElement) {
		String packageName = psiElement.getText();

		if (StringUtil.isEmptyOrSpaces(packageName)) {
			return PsiReference.EMPTY_ARRAY;
		}

		int offset = 0;

		if (packageName.charAt(0) == '!') {
			packageName = packageName.substring(1);
			offset = 1;
		}

		int size = packageName.length() - 1;

		if (packageName.charAt(size) == '?') {
			packageName = packageName.substring(0, size);
		}

		Project project = psiElement.getProject();

		Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);

		GlobalSearchScope scope;

		if (module == null) {
			scope = ProjectScope.getAllScope(project);
		}
		else {
			scope = module.getModuleWithDependenciesAndLibrariesScope(false);
		}

		PackageReferenceSet packageReferenceSet = new PackageReferenceSet(packageName, psiElement, offset, scope) {

			@Override
			public Collection<PsiPackage> resolvePackageName(@Nullable PsiPackage context, String packageName) {
				if (context == null) {
					return Collections.emptyList();
				}

				packageName = packageName.replaceAll("\\s+", "");

				if (packageName.length() > 0) {
					if (packageName.charAt(0) == '!') {
						packageName = packageName.substring(1);
					}

					final String unwrappedPackageName = packageName;

					return ContainerUtil.filter(
						context.getSubPackages(getResolveScope()),
						psiPackage -> unwrappedPackageName.equalsIgnoreCase(psiPackage.getName()));
				}

				return Collections.emptyList();
			}

		};

		List<PsiPackageReference> psiPackageReferences = packageReferenceSet.getReferences();

		return psiPackageReferences.toArray(new PsiPackageReference[0]);
	}

	@NotNull
	public static PsiDirectory[] resolvePackage(@NotNull PsiElement psiElement, @NotNull String packageName) {
		Project project = psiElement.getProject();

		Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);

		GlobalSearchScope scope;

		if (module == null) {
			scope = ProjectScope.getAllScope(project);
		}
		else {
			scope = module.getModuleWithDependenciesAndLibrariesScope(false);
		}

		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);

		PsiPackage psiPackage = javaPsiFacade.findPackage(packageName);

		if (psiPackage == null) {
			return PsiDirectory.EMPTY_ARRAY;
		}

		return psiPackage.getDirectories(scope);
	}

}