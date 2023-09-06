/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;

import com.liferay.ide.idea.bnd.psi.Attribute;
import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;
import com.liferay.ide.idea.bnd.psi.Clause;
import com.liferay.ide.idea.bnd.psi.Directive;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Charles Wu
 */
public class ClauseImpl extends ASTWrapperPsiElement implements Clause {

	public ClauseImpl(ASTNode node) {
		super(node);
	}

	@Nullable
	@Override
	public Attribute getAttribute(@NotNull String name) {
		for (Attribute child = PsiTreeUtil.findChildOfType(this, Attribute.class); child != null;
			 child = PsiTreeUtil.getNextSiblingOfType(child, Attribute.class)) {

			if (name.equals(child.getName())) {
				return child;
			}
		}

		return null;
	}

	@NotNull
	@Override
	public List<Attribute> getAttributes() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, Attribute.class);
	}

	@Override
	public Directive getDirective(@NotNull String name) {
		for (Directive child = PsiTreeUtil.findChildOfType(this, Directive.class); child != null;
			 child = PsiTreeUtil.getNextSiblingOfType(child, Directive.class)) {

			if (name.equals(child.getName())) {
				return child;
			}
		}

		return null;
	}

	@NotNull
	@Override
	public List<Directive> getDirectives() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, Directive.class);
	}

	@NotNull
	@Override
	public String getUnwrappedText() {
		String str = getText().replaceAll("(?s)\\s*\n\\s*", "");

		return str.trim();
	}

	@Override
	public BndHeaderValuePart getValue() {
		return findChildByClass(BndHeaderValuePart.class);
	}

	@Override
	public String toString() {
		return "Clause";
	}

}