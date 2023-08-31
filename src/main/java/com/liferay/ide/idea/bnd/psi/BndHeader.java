/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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