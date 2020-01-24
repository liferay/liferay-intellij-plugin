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

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.util.PsiTreeUtil;

import com.liferay.ide.idea.bnd.BndFileType;
import com.liferay.ide.idea.bnd.BndLanguage;
import com.liferay.ide.idea.bnd.psi.BndFile;
import com.liferay.ide.idea.bnd.psi.BndHeader;
import com.liferay.ide.idea.bnd.psi.BndSection;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class BndFileImpl extends PsiFileBase implements BndFile {

	public BndFileImpl(FileViewProvider viewProvider) {
		super(viewProvider, BndLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public FileType getFileType() {
		return BndFileType.INSTANCE;
	}

	@Nullable
	@Override
	public BndHeader getHeader(@NotNull String name) {
		BndHeader child = PsiTreeUtil.findChildOfType(getFirstChild(), BndHeader.class);

		while (child != null) {
			if (name.equals(child.getName())) {
				return child;
			}

			child = PsiTreeUtil.getNextSiblingOfType(child, BndHeader.class);
		}

		return null;
	}

	@NotNull
	@Override
	public List<BndHeader> getHeaders() {
		return PsiTreeUtil.getChildrenOfTypeAsList(getFirstChild(), BndHeader.class);
	}

	@Nullable
	@Override
	public BndSection getMainSection() {
		return findChildByClass(BndSection.class);
	}

	@NotNull
	@Override
	public List<BndSection> getSections() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, BndSection.class);
	}

	@Override
	public String toString() {
		return "BndFile:" + getName();
	}

}