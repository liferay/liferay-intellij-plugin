/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.server.portal;

import com.intellij.openapi.projectRoots.Sdk;

import java.nio.file.Path;

/**
 * @author Simon Jiang
 */
public interface PortalBundle {

	public Path getAppServerDir();

	public String getDisplayName();

	public Path getLiferayHome();

	public String getMainClass();

	public Path[] getRuntimeClasspath();

	public String[] getRuntimeStartProgArgs();

	public String[] getRuntimeStartVMArgs(Sdk sdk);

	public String getType();

}