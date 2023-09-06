/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.psi;

import com.intellij.openapi.util.TextRange;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public interface BndHeaderValuePart extends BndHeaderValue {

	/**
	 * Returns the range to highlight in the element.
	 */
	@NotNull
	public TextRange getHighlightingRange();

}