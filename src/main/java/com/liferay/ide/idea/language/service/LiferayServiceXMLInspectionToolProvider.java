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