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

package com.liferay.ide.idea.bnd.parser;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;

import com.liferay.ide.idea.bnd.psi.BndHeader;
import com.liferay.ide.idea.bnd.psi.BndHeaderValue;
import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;
import com.liferay.ide.idea.bnd.psi.Clause;
import com.liferay.ide.idea.util.LiferayAnnotationUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.osgi.framework.Version;

/**
 * @author Dominik Marks
 */
public class BundleVersionParser extends BndHeaderParser {

	public static final BundleVersionParser INSTANCE = new BundleVersionParser();

	@Override
	public boolean annotate(@NotNull BndHeader bndHeader, @NotNull AnnotationHolder annotationHolder) {
		BndHeaderValue value = bndHeader.getBndHeaderValue();

		if (value instanceof Clause) {
			Clause clause = (Clause)value;

			value = clause.getValue();
		}

		if (value instanceof BndHeaderValuePart) {
			try {
				new Version(value.getUnwrappedText());
			}
			catch (IllegalArgumentException illegalArgumentException) {
				BndHeaderValuePart bndHeaderValuePart = (BndHeaderValuePart)value;

				TextRange range = bndHeaderValuePart.getHighlightingRange();

				LiferayAnnotationUtil.createAnnotation(
					annotationHolder, HighlightSeverity.ERROR, illegalArgumentException.getMessage(), range);

				return true;
			}
		}

		return false;
	}

	@Nullable
	@Override
	public Object getConvertedValue(@NotNull BndHeader bndHeader) {
		BndHeaderValue value = bndHeader.getBndHeaderValue();

		if (value instanceof Clause) {
			Clause clause = (Clause)value;

			value = clause.getValue();
		}

		if (value instanceof BndHeaderValuePart) {
			try {
				return new Version(value.getUnwrappedText());
			}
			catch (IllegalArgumentException illegalArgumentException) {
			}
		}

		return null;
	}

	private BundleVersionParser() {
	}

}