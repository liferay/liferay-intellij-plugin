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
	//TODO add a test: rename a class and check if the class is also renamed in the bnd.bnd file
	//TODO add a test: rename a package and check if the class is also renamed in the bnd.bnd file

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