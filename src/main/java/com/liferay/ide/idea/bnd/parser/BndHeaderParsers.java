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

import com.liferay.ide.idea.bnd.LiferayBndConstants;

import java.util.HashMap;
import java.util.Map;

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

			for (String header : _CLASS_REFERENCE_PROPERTIES) {
				put(header, ClassReferenceParser.INSTANCE);
			}

			for (String header : _FILE_REFERENCE_PROPERTIES) {
				put(header, FileReferenceParser.INSTANCE);
			}

			for (String header : _DEFAULT_HEADER_PROPERTIES) {
				put(header, BndHeaderParser.INSTANCE);
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

	private static final String[] _CLASS_REFERENCE_PROPERTIES = {
		LiferayBndConstants.PLUGIN_BUNDLE, LiferayBndConstants.PLUGIN_JSP, LiferayBndConstants.PLUGIN_NPM,
		LiferayBndConstants.PLUGIN_RESOURCEBUNDLE, LiferayBndConstants.PLUGIN_SASS, LiferayBndConstants.PLUGIN_SERVICE,
		LiferayBndConstants.PLUGIN_SPRING, aQute.bnd.osgi.Constants.TESTCASES
	};

	private static final String[] _DEFAULT_HEADER_PROPERTIES = {
		LiferayBndConstants.JSP, LiferayBndConstants.SASS, LiferayBndConstants.LIFERAY_SERVICE_XML,
		LiferayBndConstants.LIFERAY_RELENG_MODULE_GROUP_DESCRIPTION,
		LiferayBndConstants.LIFERAY_RELENG_MODULE_GROUP_TITLE, LiferayBndConstants.LIFERAY_REQUIRE_SCHEMA_VERSION,
		LiferayBndConstants.LIFERAY_SERVICE, LiferayBndConstants.DYNAMIC_IMPORT_PACKAGE,
		LiferayBndConstants.LIFERAY_MODULES_COMPAT_ADAPTERS
	};

	private static final String[] _FILE_REFERENCE_PROPERTIES = {
		LiferayBndConstants.LIFERAY_JS_CONFIG, LiferayBndConstants.LIFERAY_CONFIGURATION_PATH
	};

}