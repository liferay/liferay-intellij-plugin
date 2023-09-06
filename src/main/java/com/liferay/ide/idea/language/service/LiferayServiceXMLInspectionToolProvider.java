/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.service;

import com.intellij.codeInspection.InspectionToolProvider;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
@SuppressWarnings("unchecked")
public class LiferayServiceXMLInspectionToolProvider implements InspectionToolProvider {

	@NotNull
	@Override
	public Class[] getInspectionClasses() {
		return new Class<?>[] {
			LiferayServiceXMLPrimaryKeyColumnInspection.class, LiferayServiceXMLNamespaceInspection.class,
			LiferayServiceXMLExceptionNameInspection.class, LiferayServiceXMLEntityUuidInspection.class,
			LiferayServiceXMLDuplicateColumnInspection.class, LiferayServiceXMLDuplicateEntityInspection.class,
			LiferayServiceXMLDuplicateExceptionInspection.class, LiferayServiceXMLDuplicateFinderInspection.class
		};
	}

}