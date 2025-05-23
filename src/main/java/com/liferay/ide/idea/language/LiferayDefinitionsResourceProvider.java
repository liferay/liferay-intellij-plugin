/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;

/**
 * @author Dominik Marks
 * @author Joye Luo
 */
public class LiferayDefinitionsResourceProvider implements StandardResourceProvider {

	public static final String XML_NAMESPACE_JCP_PORTLET_APP_3_0 = "http://xmlns.jcp.org/xml/ns/portlet";

	public static final String XML_NAMESPACE_LIFERAY_DISPLAY_7_0_0 =
		"http://www.liferay.com/dtd/liferay-display_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_DISPLAY_7_1_0 =
		"http://www.liferay.com/dtd/liferay-display_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_DISPLAY_7_2_0 =
		"http://www.liferay.com/dtd/liferay-display_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_DISPLAY_7_3_0 =
		"http://www.liferay.com/dtd/liferay-display_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_DISPLAY_7_4_0 =
		"http://www.liferay.com/dtd/liferay-display_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_FRIENDLY_URL_ROUTES_7_0_0 =
		"http://www.liferay.com/dtd/liferay-friendly-url-routes_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_FRIENDLY_URL_ROUTES_7_1_0 =
		"http://www.liferay.com/dtd/liferay-friendly-url-routes_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_FRIENDLY_URL_ROUTES_7_2_0 =
		"http://www.liferay.com/dtd/liferay-friendly-url-routes_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_FRIENDLY_URL_ROUTES_7_3_0 =
		"http://www.liferay.com/dtd/liferay-friendly-url-routes_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_FRIENDLY_URL_ROUTES_7_4_0 =
		"http://www.liferay.com/dtd/liferay-friendly-url-routes_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_HOOK_7_0_0 = "http://www.liferay.com/dtd/liferay-hook_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_HOOK_7_1_0 = "http://www.liferay.com/dtd/liferay-hook_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_HOOK_7_2_0 = "http://www.liferay.com/dtd/liferay-hook_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_HOOK_7_3_0 = "http://www.liferay.com/dtd/liferay-hook_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_HOOK_7_4_0 = "http://www.liferay.com/dtd/liferay-hook_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_0_0 =
		"http://www.liferay.com/dtd/liferay-layout-templates_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_1_0 =
		"http://www.liferay.com/dtd/liferay-layout-templates_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_2_0 =
		"http://www.liferay.com/dtd/liferay-layout-templates_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_3_0 =
		"http://www.liferay.com/dtd/liferay-layout-templates_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_4_0 =
		"http://www.liferay.com/dtd/liferay-layout-templates_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_0_0 =
		"http://www.liferay.com/dtd/liferay-look-and-feel_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_1_0 =
		"http://www.liferay.com/dtd/liferay-look-and-feel_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_2_0 =
		"http://www.liferay.com/dtd/liferay-look-and-feel_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_3_0 =
		"http://www.liferay.com/dtd/liferay-look-and-feel_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_4_0 =
		"http://www.liferay.com/dtd/liferay-look-and-feel_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PLUGIN_PACKAGE_7_0_0 =
		"http://www.liferay.com/dtd/liferay-plugin-package_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PLUGIN_PACKAGE_7_1_0 =
		"http://www.liferay.com/dtd/liferay-plugin-package_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PLUGIN_PACKAGE_7_2_0 =
		"http://www.liferay.com/dtd/liferay-plugin-package_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PLUGIN_PACKAGE_7_3_0 =
		"http://www.liferay.com/dtd/liferay-plugin-package_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PLUGIN_PACKAGE_7_4_0 =
		"http://www.liferay.com/dtd/liferay-plugin-package_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PLUGIN_REPOSITORY_7_0_0 =
		"http://www.liferay.com/dtd/liferay-plugin-repository_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PLUGIN_REPOSITORY_7_1_0 =
		"http://www.liferay.com/dtd/liferay-plugin-repository_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PLUGIN_REPOSITORY_7_2_0 =
		"http://www.liferay.com/dtd/liferay-plugin-repository_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PLUGIN_REPOSITORY_7_3_0 =
		"http://www.liferay.com/dtd/liferay-plugin-repository_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PLUGIN_REPOSITORY_7_4_0 =
		"http://www.liferay.com/dtd/liferay-plugin-repository_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PORTLET_APP_7_0_0 =
		"http://www.liferay.com/dtd/liferay-portlet-app_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PORTLET_APP_7_1_0 =
		"http://www.liferay.com/dtd/liferay-portlet-app_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PORTLET_APP_7_2_0 =
		"http://www.liferay.com/dtd/liferay-portlet-app_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PORTLET_APP_7_3_0 =
		"http://www.liferay.com/dtd/liferay-portlet-app_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_PORTLET_APP_7_4_0 =
		"http://www.liferay.com/dtd/liferay-portlet-app_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_0_0 =
		"http://www.liferay.com/dtd/liferay-resource-action-mapping_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_1_0 =
		"http://www.liferay.com/dtd/liferay-resource-action-mapping_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_2_0 =
		"http://www.liferay.com/dtd/liferay-resource-action-mapping_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_3_0 =
		"http://www.liferay.com/dtd/liferay-resource-action-mapping_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_4_0 =
		"http://www.liferay.com/dtd/liferay-resource-action-mapping_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_0_0 =
		"http://www.liferay.com/dtd/liferay-service-builder_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_1_0 =
		"http://www.liferay.com/dtd/liferay-service-builder_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_2_0 =
		"http://www.liferay.com/dtd/liferay-service-builder_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_3_0 =
		"http://www.liferay.com/dtd/liferay-service-builder_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_4_0 =
		"http://www.liferay.com/dtd/liferay-service-builder_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_SOCIAL_7_0_0 =
		"http://www.liferay.com/dtd/liferay-social_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_SOCIAL_7_1_0 =
		"http://www.liferay.com/dtd/liferay-social_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_SOCIAL_7_2_0 =
		"http://www.liferay.com/dtd/liferay-social_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_SOCIAL_7_3_0 =
		"http://www.liferay.com/dtd/liferay-social_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_SOCIAL_7_4_0 =
		"http://www.liferay.com/dtd/liferay-social_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_THEME_LOADER_7_0_0 =
		"http://www.liferay.com/dtd/liferay-theme-loader_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_THEME_LOADER_7_1_0 =
		"http://www.liferay.com/dtd/liferay-theme-loader_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_THEME_LOADER_7_2_0 =
		"http://www.liferay.com/dtd/liferay-theme-loader_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_THEME_LOADER_7_3_0 =
		"http://www.liferay.com/dtd/liferay-theme-loader_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_THEME_LOADER_7_4_0 =
		"http://www.liferay.com/dtd/liferay-theme-loader_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_USER_NOTIFICATION_DEFINITIONS_7_0_0 =
		"http://www.liferay.com/dtd/liferay-user-notification-definitions_7_0_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_USER_NOTIFICATION_DEFINITIONS_7_1_0 =
		"http://www.liferay.com/dtd/liferay-user-notification-definitions_7_1_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_USER_NOTIFICATION_DEFINITIONS_7_2_0 =
		"http://www.liferay.com/dtd/liferay-user-notification-definitions_7_2_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_USER_NOTIFICATION_DEFINITIONS_7_3_0 =
		"http://www.liferay.com/dtd/liferay-user-notification-definitions_7_3_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_USER_NOTIFICATION_DEFINITIONS_7_4_0 =
		"http://www.liferay.com/dtd/liferay-user-notification-definitions_7_4_0.dtd";

	public static final String XML_NAMESPACE_LIFERAY_WORKFLOW_DEFINITION_7_0_0 =
		"http://www.liferay.com/dtd/liferay-workflow-definition_7_0_0.xsd";

	public static final String XML_NAMESPACE_LIFERAY_WORKFLOW_DEFINITION_7_1_0 =
		"http://www.liferay.com/dtd/liferay-workflow-definition_7_1_0.xsd";

	public static final String XML_NAMESPACE_LIFERAY_WORKFLOW_DEFINITION_7_2_0 =
		"http://www.liferay.com/dtd/liferay-workflow-definition_7_2_0.xsd";

	public static final String XML_NAMESPACE_LIFERAY_WORKFLOW_DEFINITION_7_3_0 =
		"http://www.liferay.com/dtd/liferay-workflow-definition_7_3_0.xsd";

	public static final String XML_NAMESPACE_LIFERAY_WORKFLOW_DEFINITION_7_4_0 =
		"http://www.liferay.com/dtd/liferay-workflow-definition_7_4_0.xsd";

	public static final String XML_NAMESPACE_SUN_PORTLET_APP_2_0 =
		"http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd";

	public void registerResources(ResourceRegistrar resourceRegistrar) {
		ClassLoader classLoader = LiferayDefinitionsResourceProvider.class.getClassLoader();

		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_DISPLAY_7_0_0, "definitions/dtd/liferay-display_7_0_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_DISPLAY_7_1_0, "definitions/dtd/liferay-display_7_1_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_DISPLAY_7_2_0, "definitions/dtd/liferay-display_7_2_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_DISPLAY_7_3_0, "definitions/dtd/liferay-display_7_3_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_DISPLAY_7_4_0, "definitions/dtd/liferay-display_7_4_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_FRIENDLY_URL_ROUTES_7_0_0, "definitions/dtd/liferay-friendly-url-routes_7_0_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_FRIENDLY_URL_ROUTES_7_1_0, "definitions/dtd/liferay-friendly-url-routes_7_1_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_FRIENDLY_URL_ROUTES_7_2_0, "definitions/dtd/liferay-friendly-url-routes_7_2_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_FRIENDLY_URL_ROUTES_7_3_0, "definitions/dtd/liferay-friendly-url-routes_7_3_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_FRIENDLY_URL_ROUTES_7_4_0, "definitions/dtd/liferay-friendly-url-routes_7_4_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_HOOK_7_0_0, "definitions/dtd/liferay-hook_7_0_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_HOOK_7_1_0, "definitions/dtd/liferay-hook_7_1_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_HOOK_7_2_0, "definitions/dtd/liferay-hook_7_2_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_HOOK_7_3_0, "definitions/dtd/liferay-hook_7_3_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_HOOK_7_4_0, "definitions/dtd/liferay-hook_7_4_0.dtd", classLoader);

		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_0_0, "definitions/dtd/liferay-layout-templates_7_0_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_1_0, "definitions/dtd/liferay-layout-templates_7_1_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_2_0, "definitions/dtd/liferay-layout-templates_7_2_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_3_0, "definitions/dtd/liferay-layout-templates_7_3_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_LAYOUT_TEMPLATES_7_4_0, "definitions/dtd/liferay-layout-templates_7_4_0.dtd",
			classLoader);

		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_0_0, "definitions/dtd/liferay-look-and-feel_7_0_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_1_0, "definitions/dtd/liferay-look-and-feel_7_1_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_2_0, "definitions/dtd/liferay-look-and-feel_7_2_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_3_0, "definitions/dtd/liferay-look-and-feel_7_3_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_LOOK_AND_FEEL_7_4_0, "definitions/dtd/liferay-look-and-feel_7_4_0.dtd", classLoader);

		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PLUGIN_PACKAGE_7_0_0, "definitions/dtd/liferay-plugin-package_7_0_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PLUGIN_PACKAGE_7_1_0, "definitions/dtd/liferay-plugin-package_7_1_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PLUGIN_PACKAGE_7_2_0, "definitions/dtd/liferay-plugin-package_7_2_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PLUGIN_PACKAGE_7_3_0, "definitions/dtd/liferay-plugin-package_7_3_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PLUGIN_PACKAGE_7_4_0, "definitions/dtd/liferay-plugin-package_7_4_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PLUGIN_REPOSITORY_7_0_0, "definitions/dtd/liferay-plugin-repository_7_0_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PLUGIN_REPOSITORY_7_1_0, "definitions/dtd/liferay-plugin-repository_7_1_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PLUGIN_REPOSITORY_7_2_0, "definitions/dtd/liferay-plugin-repository_7_2_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PLUGIN_REPOSITORY_7_3_0, "definitions/dtd/liferay-plugin-repository_7_3_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PLUGIN_REPOSITORY_7_4_0, "definitions/dtd/liferay-plugin-repository_7_4_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PORTLET_APP_7_0_0, "definitions/dtd/liferay-portlet-app_7_0_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PORTLET_APP_7_1_0, "definitions/dtd/liferay-portlet-app_7_1_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PORTLET_APP_7_2_0, "definitions/dtd/liferay-portlet-app_7_2_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PORTLET_APP_7_3_0, "definitions/dtd/liferay-portlet-app_7_3_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_PORTLET_APP_7_4_0, "definitions/dtd/liferay-portlet-app_7_4_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_0_0,
			"definitions/dtd/liferay-resource-action-mapping_7_0_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_1_0,
			"definitions/dtd/liferay-resource-action-mapping_7_1_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_2_0,
			"definitions/dtd/liferay-resource-action-mapping_7_2_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_3_0,
			"definitions/dtd/liferay-resource-action-mapping_7_3_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_RESOURCE_ACTION_MAPPING_7_4_0,
			"definitions/dtd/liferay-resource-action-mapping_7_4_0.dtd", classLoader);

		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_0_0, "definitions/dtd/liferay-service-builder_7_0_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_1_0, "definitions/dtd/liferay-service-builder_7_1_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_2_0, "definitions/dtd/liferay-service-builder_7_2_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_3_0, "definitions/dtd/liferay-service-builder_7_3_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_SERVICE_BUILDER_7_4_0, "definitions/dtd/liferay-service-builder_7_4_0.dtd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_SOCIAL_7_0_0, "definitions/dtd/liferay-social_7_0_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_SOCIAL_7_1_0, "definitions/dtd/liferay-social_7_1_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_SOCIAL_7_2_0, "definitions/dtd/liferay-social_7_2_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_SOCIAL_7_3_0, "definitions/dtd/liferay-social_7_3_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_SOCIAL_7_4_0, "definitions/dtd/liferay-social_7_4_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_THEME_LOADER_7_0_0, "definitions/dtd/liferay-theme-loader_7_0_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_THEME_LOADER_7_1_0, "definitions/dtd/liferay-theme-loader_7_1_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_THEME_LOADER_7_2_0, "definitions/dtd/liferay-theme-loader_7_2_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_THEME_LOADER_7_3_0, "definitions/dtd/liferay-theme-loader_7_3_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_THEME_LOADER_7_4_0, "definitions/dtd/liferay-theme-loader_7_4_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_USER_NOTIFICATION_DEFINITIONS_7_0_0,
			"definitions/dtd/liferay-user-notification-definitions_7_0_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_USER_NOTIFICATION_DEFINITIONS_7_1_0,
			"definitions/dtd/liferay-user-notification-definitions_7_1_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_USER_NOTIFICATION_DEFINITIONS_7_2_0,
			"definitions/dtd/liferay-user-notification-definitions_7_2_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_USER_NOTIFICATION_DEFINITIONS_7_3_0,
			"definitions/dtd/liferay-user-notification-definitions_7_3_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_USER_NOTIFICATION_DEFINITIONS_7_4_0,
			"definitions/dtd/liferay-user-notification-definitions_7_4_0.dtd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_WORKFLOW_DEFINITION_7_0_0, "definitions/xsd/liferay-workflow-definition_7_0_0.xsd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_WORKFLOW_DEFINITION_7_1_0, "definitions/xsd/liferay-workflow-definition_7_1_0.xsd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_WORKFLOW_DEFINITION_7_2_0, "definitions/xsd/liferay-workflow-definition_7_2_0.xsd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_WORKFLOW_DEFINITION_7_3_0, "definitions/xsd/liferay-workflow-definition_7_3_0.xsd",
			classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_LIFERAY_WORKFLOW_DEFINITION_7_4_0, "definitions/xsd/liferay-workflow-definition_7_4_0.xsd",
			classLoader);

		resourceRegistrar.addStdResource(
			XML_NAMESPACE_SUN_PORTLET_APP_2_0, "definitions/xsd/portlet-app_2_0.xsd", classLoader);
		resourceRegistrar.addStdResource(
			XML_NAMESPACE_JCP_PORTLET_APP_3_0, "definitions/xsd/portlet-app_3_0.xsd", classLoader);
	}

}