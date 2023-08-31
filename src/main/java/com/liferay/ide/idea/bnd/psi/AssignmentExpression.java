/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.psi;

import com.intellij.psi.PsiNamedElement;

/**
 * @author Charles Wu
 */
public interface AssignmentExpression extends PsiNamedElement {

	public BndHeaderValuePart getNameElement();

	public String getValue();

	public BndHeaderValuePart getValueElement();

}