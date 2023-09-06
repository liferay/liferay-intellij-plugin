/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.parser;

import aQute.bnd.osgi.Constants;

import com.liferay.ide.idea.bnd.LiferayBndConstants;

/**
 * @author Dominik Marks
 * @author Simon Jiang
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
		LiferayBndConstants.LIFERAY_MODULES_COMPAT_ADAPTERS, LiferayBndConstants.WEB_CONTEXT_PATH
	};

	public static final String[] FILE_REFERENCE_PROPERTIES = {
		LiferayBndConstants.LIFERAY_JS_CONFIG, LiferayBndConstants.LIFERAY_CONFIGURATION_PATH
	};

}