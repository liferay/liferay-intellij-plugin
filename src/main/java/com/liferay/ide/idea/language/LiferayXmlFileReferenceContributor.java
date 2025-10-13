/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.SoftFileReferenceSet;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;

/**
 * Reference contributor for file references in Liferay specific XML files
 *
 * @author Dominik Marks
 */
public class LiferayXmlFileReferenceContributor extends PsiReferenceContributor {

	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
		PsiElementPattern.Capture<PsiElement> capture = PlatformPatterns.psiElement(PsiElement.class);

		capture = capture.and(new LiferayXmlFileReferenceFilterPattern());

		registrar.registerReferenceProvider(
			capture,
			new PsiReferenceProvider() {

				@NotNull
				@Override
				public PsiReference[] getReferencesByElement(
					@NotNull PsiElement element, @NotNull ProcessingContext context) {

					TextRange originalRange = element.getTextRange();
					String valueString;
					int startInElement;

					if (element instanceof XmlAttributeValue) {
						TextRange valueRange = TextRange.create(1, originalRange.getLength() - 1);

						valueString = valueRange.substring(element.getText());

						startInElement = 1;
					}
					else {
						TextRange valueRange = TextRange.create(0, originalRange.getLength());

						valueString = valueRange.substring(element.getText());

						startInElement = 0;
					}

					FileReferenceSet fileReferenceSet = new SoftFileReferenceSet(
						valueString, element, startInElement, null, SystemInfo.isFileSystemCaseSensitive, false);

					fileReferenceSet.addCustomization(
						FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION,
						LiferayXmlFileReferenceContributor::_getModuleResourceDirectories);

					return fileReferenceSet.getAllReferences();
				}

			});
	}

	private static Collection<PsiFileSystemItem> _getModuleResourceDirectories(final @NotNull PsiFile file) {
		final VirtualFile virtualFile = file.getVirtualFile();

		if (virtualFile == null) {
			return Collections.emptyList();
		}

		final PsiDirectory parent = file.getParent();

		final Module module = ModuleUtilCore.findModuleForPsiElement((parent == null) ? file : parent);

		if (module == null) {
			return Collections.emptyList();
		}

		PsiManager psiManager = PsiManager.getInstance(module.getProject());

		Collection<PsiFileSystemItem> moduleResourceDirectories = new ArrayList<>();

		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		for (VirtualFile sourceRoot : moduleRootManager.getSourceRoots()) {
			PsiDirectory psiDirectory = psiManager.findDirectory(sourceRoot);

			if (psiDirectory != null) {
				moduleResourceDirectories.add(psiDirectory);
			}
		}

		return moduleResourceDirectories;
	}

}