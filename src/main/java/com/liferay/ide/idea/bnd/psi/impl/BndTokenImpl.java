/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.psi.impl;

import com.intellij.psi.impl.source.tree.LeafPsiElement;

import com.liferay.ide.idea.bnd.psi.BndToken;
import com.liferay.ide.idea.bnd.psi.BndTokenType;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class BndTokenImpl extends LeafPsiElement implements BndToken {

	public BndTokenImpl(@NotNull BndTokenType type, CharSequence text) {
		super(type, text);
	}

	@Override
	public BndTokenType getTokenType() {
		return (BndTokenType)getElementType();
	}

	@Override
	public String toString() {
		return "BndToken:" + getTokenType();
	}

}