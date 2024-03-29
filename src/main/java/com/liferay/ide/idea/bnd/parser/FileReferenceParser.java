/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.parser;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
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
import com.liferay.ide.idea.util.LiferayAnnotationUtil;

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
			LiferayAnnotationUtil.createAnnotation(
				holder, HighlightSeverity.ERROR, ManifestBundle.message("header.reference.invalid"),
				bndHeaderValuePart.getHighlightingRange());

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

		LiferayAnnotationUtil.createAnnotation(
			holder, HighlightSeverity.ERROR, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);

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