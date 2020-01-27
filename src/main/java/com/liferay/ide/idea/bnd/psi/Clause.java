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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Charles Wu
 */
public interface Clause extends BndHeaderValue {

	/**
	 * Returns the attribute with the given name.
	 */
	@Nullable
	public Attribute getAttribute(@NotNull String name);

	/**
	 * Returns all attributes of this clause.
	 */
	@NotNull
	public List<Attribute> getAttributes();

	/**
	 * Returns the directive with the given name.
	 */
	@Nullable
	public Directive getDirective(@NotNull String name);

	/**
	 * Returns all directives of this clause.
	 */
	@NotNull
	public List<Directive> getDirectives();

	/**
	 * Returns the value of this clause.
	 */
	@Nullable
	public BndHeaderValuePart getValue();

}