/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

import com.liferay.ide.idea.bnd.psi.BndSection;

/**
 * @author Dominik Marks
 */
public class BndSectionImpl extends ASTWrapperPsiElement implements BndSection {

	public BndSectionImpl(ASTNode node) {
		super(node);
	}

	@Override
	public String toString() {
		return "BndSection";
	}

}