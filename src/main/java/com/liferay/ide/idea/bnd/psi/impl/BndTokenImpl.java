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