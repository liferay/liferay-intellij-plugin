/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules.springmvcportlet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gregory Amerson
 */
@SuppressWarnings("serial")
public class SpringMVCPortletProjectConstants {

	public static final String[] SPRING_FRAMEWORK = {"PortletMVC4Spring", "Spring Portlet MVC"};

	public static final String[] SPRING_FRAMEWORK_DEPENDENCIES = {"Embedded", "Provided"};

	public static final String[] SPRING_VIEW_TYPE = {"Jsp", "Thymeleaf"};

	public static final Map<String, String> springDependenciesInjectors = new HashMap<String, String>() {
		{
			put("DS", new String("ds"));
			put("Spring", new String("springmvcportlet"));
		}
	};
	public static final Map<String, String> springFrameworkDependeices = new HashMap<String, String>() {
		{
			put(SPRING_FRAMEWORK_DEPENDENCIES[0], new String("embedded"));
			put(SPRING_FRAMEWORK_DEPENDENCIES[1], new String("provided"));
		}
	};
	public static final Map<String, String> springFrameworks = new HashMap<String, String>() {
		{
			put(SPRING_FRAMEWORK[0], new String("portletmvc4spring"));
			put(SPRING_FRAMEWORK[1], new String("springportletmvc"));
		}
	};
	public static final Map<String, String> springViewTypes = new HashMap<String, String>() {
		{
			put(SPRING_VIEW_TYPE[0], new String("jsp"));
			put(SPRING_VIEW_TYPE[1], new String("thymeleaf"));
		}
	};

}