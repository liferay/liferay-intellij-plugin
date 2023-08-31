/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

import com.liferay.ide.idea.bnd.BndLanguage;
import com.liferay.ide.idea.bnd.psi.impl.AttributeImpl;
import com.liferay.ide.idea.bnd.psi.impl.BndHeaderImpl;
import com.liferay.ide.idea.bnd.psi.impl.BndHeaderValuePartImpl;
import com.liferay.ide.idea.bnd.psi.impl.BndSectionImpl;
import com.liferay.ide.idea.bnd.psi.impl.ClauseImpl;
import com.liferay.ide.idea.bnd.psi.impl.DirectiveImpl;

/**
 * @author Charles Wu
 */
public abstract class BndElementType extends IElementType {

	public static final IElementType ATTRIBUTE = new BndElementType("ATTRIBUTE") {

		@Override
		public PsiElement createPsi(ASTNode node) {
			return new AttributeImpl(node);
		}

	};

	public static final IElementType CLAUSE = new BndElementType("CLAUSE") {

		@Override
		public PsiElement createPsi(ASTNode node) {
			return new ClauseImpl(node);
		}

	};

	public static final IElementType DIRECTIVE = new BndElementType("DIRECTIVE") {

		@Override
		public PsiElement createPsi(ASTNode node) {
			return new DirectiveImpl(node);
		}

	};

	public static final IElementType HEADER = new BndElementType("HEADER") {

		@Override
		public PsiElement createPsi(ASTNode node) {
			return new BndHeaderImpl(node);
		}

	};

	public static final IElementType HEADER_VALUE_PART = new BndElementType("HEADER_VALUE_PART") {

		@Override
		public PsiElement createPsi(ASTNode node) {
			return new BndHeaderValuePartImpl(node);
		}

	};

	public static final IElementType SECTION = new BndElementType("SECTION") {

		@Override
		public PsiElement createPsi(ASTNode node) {
			return new BndSectionImpl(node);
		}

	};

	public abstract PsiElement createPsi(ASTNode node);

	@Override
	public String toString() {
		return "bnd:" + super.toString();
	}

	private BndElementType(String name) {
		super(name, BndLanguage.INSTANCE);
	}

}