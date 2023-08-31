/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;

/**
 * @author Gregory Amerson
 */
public class PortalPropertiesConfiguration extends PropertiesConfiguration {

	public PortalPropertiesConfiguration() throws ConfigurationException {
		setDelimiterParsingDisabled(true);
	}

	@Override
	protected PropertiesConfigurationLayout createLayout() {
		return new PortalPropertiesConfigurationLayout(this);
	}

}