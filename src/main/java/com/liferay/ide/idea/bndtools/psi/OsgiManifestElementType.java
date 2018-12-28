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

package com.liferay.ide.idea.bndtools.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

import com.liferay.ide.idea.bndtools.psi.impl.AttributeImpl;
import com.liferay.ide.idea.bndtools.psi.impl.ClauseImpl;
import com.liferay.ide.idea.bndtools.psi.impl.DirectiveImpl;

import org.jetbrains.lang.manifest.psi.ManifestElementType;

/**
 * @author Charles Wu
 */
public abstract class OsgiManifestElementType extends ManifestElementType {

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

	private OsgiManifestElementType(String name) {
		super(name);
	}

}