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
public abstract class OsgiManifestElementType extends IElementType {

	public static final IElementType ATTRIBUTE = new OsgiManifestElementType("ATTRIBUTE") {

		@Override
		public PsiElement createPsi(ASTNode node) {
			return new AttributeImpl(node);
		}

	};

	public static final IElementType CLAUSE = new OsgiManifestElementType("CLAUSE") {

		@Override
		public PsiElement createPsi(ASTNode node) {
			return new ClauseImpl(node);
		}

	};

	public static final IElementType DIRECTIVE = new OsgiManifestElementType("DIRECTIVE") {

		@Override
		public PsiElement createPsi(ASTNode node) {
			return new DirectiveImpl(node);
		}

	};

	public static final IElementType HEADER = new OsgiManifestElementType("HEADER") {

		@Override
		public PsiElement createPsi(ASTNode node) {
			return new BndHeaderImpl(node);
		}

	};

	public static final IElementType HEADER_VALUE_PART = new OsgiManifestElementType("HEADER_VALUE_PART") {

		@Override
		public PsiElement createPsi(ASTNode node) {
			return new BndHeaderValuePartImpl(node);
		}

	};

	public static final IElementType SECTION = new OsgiManifestElementType("SECTION") {

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

	private OsgiManifestElementType(String name) {
		super(name, BndLanguage.INSTANCE);
	}

}