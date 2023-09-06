/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

/**
 * @author Terry Jia
 */
public class PropertiesUtil {

	public static Properties loadProperties(File f) {
		Properties properties = new Properties();

		try (FileInputStream stream = new FileInputStream(f)) {
			properties.load(stream);

			return properties;
		}
		catch (IOException ioException) {
			return null;
		}
	}

}