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

import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

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
		@NotNull PsiJavaCodeReferenceElement reference, @NotNull QuickFixActionRegistrar registrar) {

		PsiElement psiElement = reference.getElement();

		TextRange textRange = reference.getRangeInElement();

		String shortReferenceName = textRange.substring(psiElement.getText());

		Project project = psiElement.getProject();

		PsiFile containingFile = psiElement.getContainingFile();

		if (!LiferayWorkspaceUtil.isValidGradleWorkspaceProject(project) || (containingFile == null)) {
			return;
		}

		VirtualFile refVFile = containingFile.getVirtualFile();

		if (refVFile == null) {
			return;
		}

		ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

		ProjectFileIndex fileIndex = projectRootManager.getFileIndex();

		Module currentModule = fileIndex.getModuleForFile(refVFile);

		if (currentModule == null) {
			return;
		}

		PsiShortNamesCache psiShortNamesCache = PsiShortNamesCache.getInstance(project);

		PsiClass[] classes = psiShortNamesCache.getClassesByName(
			shortReferenceName, GlobalSearchScope.allScope(project));

		List<PsiClass> allowedDependencies = _filterAllowedDependencies(psiElement, classes);

		if (allowedDependencies.isEmpty()) {
			return;
		}

		String qualifiedName = _getQualifiedName(reference, shortReferenceName, containingFile);

		if (qualifiedName != null) {
			allowedDependencies.removeIf(psiClass -> !qualifiedName.equals(psiClass.getQualifiedName()));
		}

		JavaPsiFacade facade = JavaPsiFacade.getInstance(psiElement.getProject());

		PsiResolveHelper resolveHelper = facade.getResolveHelper();

		Set<Object> librariesToAdd = new HashSet<>();

		for (PsiClass psiClass : allowedDependencies) {
			if (!resolveHelper.isAccessible(psiClass, psiElement, psiClass)) {
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

			for (OrderEntry orderEntry : fileIndex.getOrderEntriesForFile(virtualFile)) {
				if (orderEntry instanceof LibraryOrderEntry) {
					final LibraryOrderEntry libraryEntry = (LibraryOrderEntry)orderEntry;

					final Library library = libraryEntry.getLibrary();

					if (library == null) {
						continue;
					}

					VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);

					if (files.length == 0) {
						continue;
					}

					final VirtualFile jar = files[0];

					if ((jar == null) || !librariesToAdd.add(library)) {
						continue;
					}
					else if (libraryEntry.isModuleLevel() && !librariesToAdd.add(jar)) {
						continue;
					}

					String libraryName = library.getName();

					if ((libraryName != null) && libraryName.startsWith(GRADLE_LIBRARY_PREFIX)) {
						IntentionAction quickFix = new GradleDependencyQuickFix(currentModule, library);

						registrar.register(quickFix);
					}
				}
			}
		}
	}

	private static List<PsiClass> _filterAllowedDependencies(PsiElement element, PsiClass[] classes) {
		DependencyValidationManager dependencyValidationManager = DependencyValidationManager.getInstance(
			element.getProject());
		PsiFile fromFile = element.getContainingFile();
		List<PsiClass> result = new ArrayList<>();

		for (PsiClass psiClass : classes) {
			PsiFile containingFile = psiClass.getContainingFile();

			if ((containingFile != null) &&
				(dependencyValidationManager.getViolatorDependencyRule(fromFile, containingFile) == null)) {

				result.add(psiClass);
			}
		}

		return result;
	}

	@Nullable
	private static String _getQualifiedName(
		@NotNull PsiJavaCodeReferenceElement reference, String shortReferenceName, PsiFile containingFile) {

		String qualifiedName = null;

		if (reference.isQualified()) {
			qualifiedName = reference.getQualifiedName();
		}
		else if (containingFile instanceof PsiJavaFile) {
			PsiImportList list = ((PsiJavaFile)containingFile).getImportList();

			if (list != null) {
				PsiImportStatementBase statement = list.findSingleImportStatement(shortReferenceName);

				if (statement != null) {
					PsiJavaCodeReferenceElement importReference = statement.getImportReference();

					qualifiedName = importReference.getQualifiedName();
				}
			}
		}

		return qualifiedName;
	}

}