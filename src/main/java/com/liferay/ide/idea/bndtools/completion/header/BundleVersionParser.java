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

package com.liferay.ide.idea.bndtools.completion.header;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.util.TextRange;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.header.HeaderParser;
import org.jetbrains.lang.manifest.header.impl.StandardHeaderParser;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;

import org.osgi.framework.Version;

/**
 * @author Charles Wu
 */
public class BundleVersionParser extends StandardHeaderParser {

	public static final HeaderParser INSTANCE = new BundleVersionParser();

	@Override
	public boolean annotate(@NotNull Header header, @NotNull AnnotationHolder holder) {
		HeaderValue value = header.getHeaderValue();

		if (value instanceof HeaderValuePart) {
			try {
				new Version(value.getUnwrappedText());
			}
			catch (IllegalArgumentException iae) {
				TextRange range = ((HeaderValuePart)value).getHighlightingRange();

				holder.createErrorAnnotation(range, iae.getMessage());

				return true;
			}
		}

		return false;
	}

	@Nullable
	@Override
	public Object getConvertedValue(@NotNull Header header) {
		HeaderValue value = header.getHeaderValue();

		if (value instanceof HeaderValuePart) {
			try {
				return new Version(value.getUnwrappedText());
			}
			catch (IllegalArgumentException iae) {
			}
		}

		return null;
	}

	private BundleVersionParser() {
	}

}