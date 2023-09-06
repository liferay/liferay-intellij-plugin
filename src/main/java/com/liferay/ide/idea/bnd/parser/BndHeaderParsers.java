/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.parser;

import aQute.bnd.osgi.Constants;

import java.util.HashMap;
import java.util.Map;

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

			for (String header : BndHeaderParsersConstants.CLASS_REFERENCE_PROPERTIES) {
				put(header, ClassReferenceParser.INSTANCE);
			}

			for (String header : BndHeaderParsersConstants.FILE_REFERENCE_PROPERTIES) {
				put(header, FileReferenceParser.INSTANCE);
			}

			for (String header : BndHeaderParsersConstants.DEFAULT_HEADER_PROPERTIES) {
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

}