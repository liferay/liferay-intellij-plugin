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

import java.util.HashMap;
import java.util.Map;

import com.liferay.ide.idea.bnd.LiferayBndConstants;
import org.gradle.internal.impldep.aQute.bnd.osgi.Constants;

/**
 * @author Dominik Marks
 */
public class BndHeaderParsers {

	public static final Map<String, BndHeaderParser> parsersMap = new HashMap<String, BndHeaderParser>() {
		{
			put(Constants.BUNDLE_ACTIVATOR, BundleActivatorParser.INSTANCE);
			put(Constants.BUNDLE_VERSION, BundleVersionParser.INSTANCE);
			put(Constants.CONDITIONAL_PACKAGE, BasePackageParser.INSTANCE);
			put(Constants.EXPORT_PACKAGE, ExportPackageParser.INSTANCE);
			put(Constants.IGNORE_PACKAGE, BasePackageParser.INSTANCE);
			put(Constants.IMPORT_PACKAGE, BasePackageParser.INSTANCE);
			put(Constants.PRIVATE_PACKAGE, BasePackageParser.INSTANCE);

			for (String header : LiferayBndConstants.CLASS_REFERENCE_PROPERTIES) {
				put(header, ClassReferenceParser.INSTANCE);
			}

			for (String header : Constants.headers) {
				if (!containsKey(header)) {
					put(header, BndHeaderParser.INSTANCE);
				}
			}

			for (String option : Constants.options) {
				if (!containsKey(option)) {
					put(option, BndHeaderParser.INSTANCE);
				}
			}
		}
	};

}