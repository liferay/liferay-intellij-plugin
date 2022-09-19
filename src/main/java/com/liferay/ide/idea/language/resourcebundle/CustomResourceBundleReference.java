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

package com.liferay.ide.idea.language.resourcebundle;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.properties.PropertiesFileProcessor;
import com.intellij.lang.properties.PropertiesReferenceManager;
import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.ResourceBundleReference;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dominik Marks
 */
public class CustomResourceBundleReference extends ResourceBundleReference {

	public CustomResourceBundleReference(PsiElement element) {
		super(element);
	}

	@Override
	public String evaluateBundleName(PsiFile psiFile) {
		String baseName = super.evaluateBundleName(psiFile);

		if (psiFile instanceof PropertiesFile) {
			PropertiesFile propertiesFile = (PropertiesFile)psiFile;

			String suffix = PropertiesUtil.getSuffix(propertiesFile);

			if (suffix.length() > 0) {
				baseName = baseName + "_" + suffix;
			}
		}

		return baseName;
	}

	@Override
	public Object[] getVariants() {
		final ProjectFileIndex projectFileIndex = ProjectFileIndex.SERVICE.getInstance(getElement().getProject());
		final PropertiesReferenceManager referenceManager = PropertiesReferenceManager.getInstance(
			getElement().getProject());

		final Set<String> bundleNames = new HashSet<>();

		final List<LookupElement> variants = new SmartList<>();

		PropertiesFileProcessor processor = (baseName, propertiesFile) -> {
			if (!bundleNames.add(baseName)) {
				return true;
			}

			final LookupElementBuilder builder = LookupElementBuilder.create(
				baseName
			).withIcon(
				AllIcons.Nodes.ResourceBundle
			);
			boolean inContent = projectFileIndex.isInContent(propertiesFile.getVirtualFile());

			variants.add(inContent ? PrioritizedLookupElement.withPriority(builder, Double.MAX_VALUE) : builder);

			return true;
		};

		referenceManager.processPropertiesFiles(_getResolveScope(), processor, this);

		return variants.toArray(LookupElement.EMPTY_ARRAY);
	}

	@Override
	public ResolveResult[] multiResolve(final boolean incompleteCode) {
		PropertiesReferenceManager referenceManager = PropertiesReferenceManager.getInstance(myElement.getProject());

		List<PropertiesFile> propertiesFiles = referenceManager.findPropertiesFiles(
			_getResolveScope(), getCanonicalText(), this);

		return PsiElementResolveResult.createResults(
			ContainerUtil.map(propertiesFiles, PropertiesFile::getContainingFile));
	}

	private GlobalSearchScope _getResolveScope() {
		Module module = ModuleUtil.findModuleForPsiElement(myElement);

		if (module != null) {
			return GlobalSearchScope.moduleScope(module);
		}

		return GlobalSearchScope.allScope(myElement.getProject());
	}

}