/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Drew Brokke
 */
public class BladeCLITest {

	@Test
	public void testGetProjectTemplatesFiltersOtherOutput() throws Exception {
		String[] executeResult = {
			"WARNING: A terminally deprecated method in sun.misc.Unsafe has been called",
			"WARNING: sun.misc.Unsafe::objectFieldOffset has been called",
			"WARNING: Please consider reporting this to the maintainers",
			"WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release", "",
			"api                           Creates a Liferay API module project.",
			"mvc-portlet                   Creates a Liferay MVC portlet module project.",
			"npm-react-portlet             Creates a Liferay MVC portlet with React support.",
			"service-wrapper               Creates a Liferay service wrapper module project.",
			"war-mvc-portlet               Creates a Liferay WAR-style MVC portlet project.", "Updates are available:",
			"-> blade update", "-> https://github.com/liferay/liferay-blade-cli", "Update available 8.0.0 -> 8.0.1",
			"Run `blade update` to install"
		};

		String[] expected = {"api", "mvc-portlet", "npm-react-portlet", "service-wrapper", "war-mvc-portlet"};

		Assert.assertArrayEquals(expected, BladeCLI.getProjectTemplates(executeResult));
	}

}