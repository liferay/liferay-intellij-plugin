/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;

import com.liferay.ide.idea.bnd.BndFileType;
import com.liferay.ide.idea.bnd.psi.BndFile;
import com.liferay.ide.idea.bnd.psi.BndHeader;
import com.liferay.ide.idea.bnd.psi.BndHeaderValue;
import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;
import com.liferay.ide.idea.bnd.psi.Clause;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class BndHeaderValuePartManipulator extends AbstractElementManipulator<BndHeaderValuePart> {

	@Override
	public BndHeaderValuePart handleContentChange(
			@NotNull BndHeaderValuePart element, @NotNull TextRange textRange, String newContent)
		throws IncorrectOperationException {

		String text = "HeaderValuePartManipulator: " + textRange.replace(element.getText(), newContent);

		PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(element.getProject());

		PsiFile psiFile = psiFileFactory.createFileFromText("bnd.bnd", BndFileType.INSTANCE, text);

		BndFile bndFile = (BndFile)psiFile;

		List<BndHeader> bndHeaders = bndFile.getHeaders();

		BndHeader bndHeader = bndHeaders.get(0);

		BndHeaderValue value = bndHeader.getBndHeaderValue();

		if (value != null) {
			BndHeaderValue bndHeaderValueReplacement = (BndHeaderValue)element.replace(value);

			if (bndHeaderValueReplacement instanceof BndHeaderValuePart) {
				return (BndHeaderValuePart)bndHeaderValueReplacement;
			}

			Clause clause = (Clause)bndHeaderValueReplacement;

			return clause.getValue();
		}

		return element;
	}

}