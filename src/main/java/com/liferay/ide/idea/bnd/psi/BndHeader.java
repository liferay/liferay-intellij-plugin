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

import com.intellij.psi.PsiNamedElement;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public interface BndHeader extends PsiNamedElement {

	/**
	 * Returns a first header value element if exists.
	 */
	@Nullable
	public BndHeaderValue getBndHeaderValue();

	/**
	 * Returns a list of all header value elements.
	 */
	@NotNull
	public List<BndHeaderValue> getBndHeaderValues();

	@NotNull
	public BndToken getBndNameElement();

	@NotNull
	@Override
	public String getName();

}