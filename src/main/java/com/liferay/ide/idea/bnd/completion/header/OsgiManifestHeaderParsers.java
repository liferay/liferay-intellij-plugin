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

package com.liferay.ide.idea.bnd.completion.header;

import aQute.bnd.osgi.Constants;

import com.intellij.util.containers.ContainerUtil;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.header.HeaderParser;
import org.jetbrains.lang.manifest.header.HeaderParserProvider;

/**
 * @author Charles Wu
 */
public class OsgiManifestHeaderParsers implements HeaderParserProvider {

	public static final Map<String, OsgiHeaderParser> parsers = new HashMap<>();

	public OsgiManifestHeaderParsers() {
		_parsers = ContainerUtil.newHashMap();

		_parsers.put(Constants.BUNDLE_VERSION, BundleVersionParser.INSTANCE);

		for (String header : Constants.headers) {
			if (!_parsers.containsKey(header)) {
				_parsers.put(header, OsgiHeaderParser.INSTANCE);
			}
		}

		for (String option : Constants.options) {
			if (!_parsers.containsKey(option)) {
				_parsers.put(option, OsgiHeaderParser.INSTANCE);
			}
		}
	}

	@NotNull
	@Override
	public Map<String, HeaderParser> getHeaderParsers() {
		return _parsers;
	}

	private final Map<String, HeaderParser> _parsers;

}