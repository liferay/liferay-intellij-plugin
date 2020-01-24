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

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

import com.liferay.ide.idea.bnd.psi.BndHeader;
import com.liferay.ide.idea.bnd.psi.BndHeaderValue;
import com.liferay.ide.idea.bnd.psi.BndToken;
import com.liferay.ide.idea.bnd.psi.BndTokenType;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class BndHeaderImpl extends ASTWrapperPsiElement implements BndHeader {

	public BndHeaderImpl(@NotNull ASTNode node) {
		super(node);
	}

	@Nullable
	@Override
	public BndHeaderValue getBndHeaderValue() {
		return PsiTreeUtil.getChildOfType(this, BndHeaderValue.class);
	}

	@NotNull
	@Override
	public List<BndHeaderValue> getBndHeaderValues() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, BndHeaderValue.class);
	}

	@NotNull
	@Override
	public BndToken getBndNameElement() {
		return (BndToken)getNode().findChildByType(BndTokenType.HEADER_NAME);
	}

	@NotNull
	@Override
	public String getName() {
		return getBndNameElement().getText();
	}

	@Override
	public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
		ASTNode astNode = getBndNameElement().getNode();

		if (astNode instanceof LeafElement) {
			LeafElement leafElement = (LeafElement)astNode;

			leafElement.replaceWithText(name);
		}

		return this;
	}

}