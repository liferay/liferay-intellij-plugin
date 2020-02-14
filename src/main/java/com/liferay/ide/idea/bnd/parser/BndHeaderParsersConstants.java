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

import aQute.bnd.osgi.Constants;

import com.liferay.ide.idea.bnd.LiferayBndConstants;

/**
 * @author Dominik Marks
 */
public class BndHeaderParsersConstants {

	public static final String[] CLASS_REFERENCE_PROPERTIES = {
		LiferayBndConstants.PLUGIN_BUNDLE, LiferayBndConstants.PLUGIN_JSP, LiferayBndConstants.PLUGIN_NPM,
		LiferayBndConstants.PLUGIN_RESOURCEBUNDLE, LiferayBndConstants.PLUGIN_SASS, LiferayBndConstants.PLUGIN_SERVICE,
		LiferayBndConstants.PLUGIN_SPRING, Constants.TESTCASES
	};

	public static final String[] DEFAULT_HEADER_PROPERTIES = {
		LiferayBndConstants.JSP, LiferayBndConstants.SASS, LiferayBndConstants.LIFERAY_SERVICE_XML,
		LiferayBndConstants.LIFERAY_RELENG_MODULE_GROUP_DESCRIPTION,
		LiferayBndConstants.LIFERAY_RELENG_MODULE_GROUP_TITLE, LiferayBndConstants.LIFERAY_REQUIRE_SCHEMA_VERSION,
		LiferayBndConstants.LIFERAY_SERVICE, LiferayBndConstants.DYNAMIC_IMPORT_PACKAGE,
		LiferayBndConstants.LIFERAY_MODULES_COMPAT_ADAPTERS
	};

	public static final String[] FILE_REFERENCE_PROPERTIES = {
		LiferayBndConstants.LIFERAY_JS_CONFIG, LiferayBndConstants.LIFERAY_CONFIGURATION_PATH
	};

}