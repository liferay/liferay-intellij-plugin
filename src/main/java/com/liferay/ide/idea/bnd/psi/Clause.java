/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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