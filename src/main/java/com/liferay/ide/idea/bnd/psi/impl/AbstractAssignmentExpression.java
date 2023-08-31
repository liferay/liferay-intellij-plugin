/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

import com.liferay.ide.idea.bnd.psi.AssignmentExpression;
import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;

import org.jetbrains.annotations.NotNull;

/**
 * @author Charles Wu
 */
public abstract class AbstractAssignmentExpression extends ASTWrapperPsiElement implements AssignmentExpression {

	public AbstractAssignmentExpression(@NotNull ASTNode node) {
		super(node);
	}

	@Override
	public String getName() {
		BndHeaderValuePart namePsi = getNameElement();

		String result = (namePsi != null) ? namePsi.getUnwrappedText() : null;

		if (result != null) {
			return result;
		}

		return "<unnamed>";
	}

	@Override
	public BndHeaderValuePart getNameElement() {
		return PsiTreeUtil.getChildOfType(this, BndHeaderValuePart.class);
	}

	@Override
	public String getValue() {
		BndHeaderValuePart valuePsi = getValueElement();

		String result = (valuePsi != null) ? valuePsi.getUnwrappedText() : null;

		if (result != null) {
			return result;
		}

		return "";
	}

	@Override
	public BndHeaderValuePart getValueElement() {
		BndHeaderValuePart namePsi = getNameElement();

		if (namePsi != null) {
			return PsiTreeUtil.getNextSiblingOfType(namePsi, BndHeaderValuePart.class);
		}

		return null;
	}

	@Override
	public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
		throw new IncorrectOperationException();
	}

}