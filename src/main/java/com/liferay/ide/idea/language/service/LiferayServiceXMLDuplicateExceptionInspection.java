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

import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLDuplicateExceptionInspection extends AbstractLiferayServiceXMLDuplicateEntryInspection {

	@Nls
	@NotNull
	@Override
	public String getDisplayName() {
		return "check for duplicate exception entries";
	}

	@Nullable
	@Override
	public String getStaticDescription() {
		return "Check for duplicate exception entries in service.xml.";
	}

	@Override
	protected boolean isSuitableXmlAttributeValue(XmlAttributeValue xmlAttributeValue) {
		return false;
	}

	@Override
	protected boolean isSuitableXmlText(XmlText xmlText) {
		return LiferayServiceXMLUtil.isExceptionTag(xmlText);
	}

}