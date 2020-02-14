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

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiReference;

import com.liferay.ide.idea.bnd.psi.BndHeader;
import com.liferay.ide.idea.bnd.psi.BndHeaderValue;
import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;
import com.liferay.ide.idea.bnd.psi.Clause;
import com.liferay.ide.idea.bnd.psi.util.BndPsiUtil;
import com.liferay.ide.idea.util.IntellijUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.ManifestBundle;

/**
 * @author Dominik Marks
 */
public class FileReferenceParser extends BndHeaderParser {

	public static final FileReferenceParser INSTANCE = new FileReferenceParser();

	@Override
	public boolean annotate(@NotNull BndHeader bndHeader, @NotNull AnnotationHolder holder) {
		BndHeaderValue bndHeaderValue = bndHeader.getBndHeaderValue();

		BndHeaderValuePart bndHeaderValuePart = null;

		if (bndHeaderValue instanceof BndHeaderValuePart) {
			bndHeaderValuePart = (BndHeaderValuePart)bndHeaderValue;
		}
		else if (bndHeaderValue instanceof Clause) {
			Clause clause = (Clause)bndHeaderValue;

			bndHeaderValuePart = clause.getValue();
		}

		if (bndHeaderValuePart == null) {
			return false;
		}

		String filePath = bndHeaderValuePart.getUnwrappedText();

		if (StringUtil.isEmptyOrSpaces(filePath)) {
			holder.createErrorAnnotation(
				bndHeaderValuePart.getHighlightingRange(), ManifestBundle.message("header.reference.invalid"));

			return true;
		}

		Module module = ModuleUtilCore.findModuleForPsiElement(bndHeader);

		if (module != null) {
			ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

			VirtualFile[] sourceRoots = moduleRootManager.getSourceRoots(false);

			String relativeFilePath = filePath;

			if (relativeFilePath.startsWith("/")) {
				relativeFilePath = relativeFilePath.substring(1);
			}

			for (VirtualFile sourceRoot : sourceRoots) {
				if (IntellijUtil.getChild(sourceRoot, relativeFilePath) != null) {
					return false;
				}
			}
		}

		String message = "Cannot resolve file '" + filePath + "'";

		Annotation annotation = holder.createErrorAnnotation(bndHeaderValuePart.getHighlightingRange(), message);

		annotation.setHighlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);

		return true;
	}

	@NotNull
	@Override
	public PsiReference[] getReferences(@NotNull BndHeaderValuePart bndHeaderValuePart) {
		if (bndHeaderValuePart.getParent() instanceof Clause) {
			return BndPsiUtil.getFileReferences(bndHeaderValuePart);
		}

		return PsiReference.EMPTY_ARRAY;
	}

}