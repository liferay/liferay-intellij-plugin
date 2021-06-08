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

package com.liferay.ide.idea.extensions;

import com.intellij.codeInsight.daemon.QuickFixActionRegistrar;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.DependencyValidationManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatementBase;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiResolveHelper;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;

import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Charles Wu
 */
public class GradleDependencyQuickFixProvider extends UnresolvedReferenceQuickFixProvider<PsiJavaCodeReferenceElement> {

	public static final String GRADLE_LIBRARY_PREFIX = GradleConstants.SYSTEM_ID.getReadableName() + ": ";

	@NotNull
	@Override
	public Class<PsiJavaCodeReferenceElement> getReferenceClass() {
		return PsiJavaCodeReferenceElement.class;
	}

	@Override
	public void registerFixes(
		@NotNull PsiJavaCodeReferenceElement psiJavaCodeReferenceElement,
		@NotNull QuickFixActionRegistrar quickFixActionRegistrar) {

		PsiElement psiElement = psiJavaCodeReferenceElement.getElement();

		Project project = psiElement.getProject();

		PsiFile containingPsiFile = psiElement.getContainingFile();

		if (!LiferayWorkspaceSupport.isValidGradleWorkspaceProject(project) || (containingPsiFile == null)) {
			return;
		}

		VirtualFile refVirtualFile = containingPsiFile.getVirtualFile();

		if (refVirtualFile == null) {
			return;
		}

		ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

		ProjectFileIndex projectFileIndex = projectRootManager.getFileIndex();

		Module currentModule = projectFileIndex.getModuleForFile(refVirtualFile);

		if (currentModule == null) {
			return;
		}

		TextRange textRange = psiJavaCodeReferenceElement.getRangeInElement();

		String shortReferenceName = textRange.substring(psiElement.getText());

		PsiShortNamesCache psiShortNamesCache = PsiShortNamesCache.getInstance(project);

		PsiClass[] psiClasses = psiShortNamesCache.getClassesByName(
			shortReferenceName, GlobalSearchScope.allScope(project));

		List<PsiClass> allowedDependenciesPsiClasses = _filterAllowedDependencies(psiElement, psiClasses);

		if (allowedDependenciesPsiClasses.isEmpty()) {
			return;
		}

		String qualifiedName = _getQualifiedName(psiJavaCodeReferenceElement, shortReferenceName, containingPsiFile);

		if (qualifiedName != null) {
			allowedDependenciesPsiClasses.removeIf(psiClass -> !qualifiedName.equals(psiClass.getQualifiedName()));
		}

		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(psiElement.getProject());

		PsiResolveHelper psiResolveHelper = javaPsiFacade.getResolveHelper();

		Set<Object> librariesToAdd = new HashSet<>();

		for (PsiClass psiClass : allowedDependenciesPsiClasses) {
			if (!psiResolveHelper.isAccessible(psiClass, psiElement, psiClass)) {
				continue;
			}

			PsiFile psiFile = psiClass.getContainingFile();

			if (psiFile == null) {
				continue;
			}

			VirtualFile virtualFile = psiFile.getVirtualFile();

			if (virtualFile == null) {
				continue;
			}

			for (OrderEntry orderEntry : projectFileIndex.getOrderEntriesForFile(virtualFile)) {
				if (orderEntry instanceof LibraryOrderEntry) {
					final LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry)orderEntry;

					final Library library = libraryOrderEntry.getLibrary();

					if (library == null) {
						continue;
					}

					VirtualFile[] virutalFiles = library.getFiles(OrderRootType.CLASSES);

					if (virutalFiles.length == 0) {
						continue;
					}

					final VirtualFile jarVirtualFile = virutalFiles[0];

					if ((jarVirtualFile == null) || !librariesToAdd.add(library)) {
						continue;
					}
					else if (libraryOrderEntry.isModuleLevel() && !librariesToAdd.add(jarVirtualFile)) {
						continue;
					}

					String libraryName = library.getName();

					if ((libraryName != null) && libraryName.startsWith(GRADLE_LIBRARY_PREFIX)) {
						IntentionAction intentionAction = new GradleDependencyQuickFix(currentModule, library);

						quickFixActionRegistrar.register(intentionAction);
					}
				}
			}
		}
	}

	private List<PsiClass> _filterAllowedDependencies(PsiElement psiElement, PsiClass[] psiClasses) {
		DependencyValidationManager dependencyValidationManager = DependencyValidationManager.getInstance(
			psiElement.getProject());
		PsiFile fromPsiFile = psiElement.getContainingFile();
		List<PsiClass> resultPsiClasses = new ArrayList<>();

		for (PsiClass psiClass : psiClasses) {
			PsiFile containingFile = psiClass.getContainingFile();

			if ((containingFile != null) &&
				(dependencyValidationManager.getViolatorDependencyRule(fromPsiFile, containingFile) == null)) {

				resultPsiClasses.add(psiClass);
			}
		}

		return resultPsiClasses;
	}

	@Nullable
	private String _getQualifiedName(
		@NotNull PsiJavaCodeReferenceElement psiJavaCodeReferenceElement, String shortReferenceName,
		PsiFile containingFile) {

		String qualifiedName = null;

		if (psiJavaCodeReferenceElement.isQualified()) {
			qualifiedName = psiJavaCodeReferenceElement.getQualifiedName();
		}
		else if (containingFile instanceof PsiJavaFile) {
			PsiJavaFile psiJavaFile = (PsiJavaFile)containingFile;

			PsiImportList psiImports = psiJavaFile.getImportList();

			if (psiImports != null) {
				PsiImportStatementBase psiImportStatementBase = psiImports.findSingleImportStatement(
					shortReferenceName);

				if (psiImportStatementBase != null) {
					PsiJavaCodeReferenceElement importReference = psiImportStatementBase.getImportReference();

					qualifiedName = importReference.getQualifiedName();
				}
			}
		}

		return qualifiedName;
	}

}