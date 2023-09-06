/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.server.portal;

import java.nio.file.Path;

/**
 * @author Simon Jiang
 */
public interface PortalBundleFactory {

	public PortalBundle create(Path location);

	public Path findAppServerPath(Path location);

	public String getType();

}