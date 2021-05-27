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

package com.liferay.ide.idea.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class WorkspaceConstants {

	public static final String BLADE_LIFERAY_VERSION_FIELD = "liferay.version.default";

	public static final String BUILD_DIR_DEFAULT = "build";

	public static final String BUNDLE_URL_PROPERTY = "liferay.workspace.bundle.url";

	public static final String DEFAULT_BUNDLE_ARTIFACT_NAME = "portal-tomcat-bundle";

	public static final String DEFAULT_BUNDLE_ARTIFACT_NAME_PROPERTY = "liferay.workspace.bundle.artifact.name";

	public static final String DEFAULT_LIFERAY_VERSION = "7.3";

	public static final String DEFAULT_PRODUCT_VERSION = "portal-7.3-ga7";

	public static final String DEFAULT_TARGET_PLATFORM_VERSION = "7.3.6";

	public static final String EXT_DIR_DEFAULT = "ext";

	public static final String EXT_DIR_PROPERTY = "liferay.workspace.ext.dir";

	public static final String HOME_DIR_DEFAULT = "bundles";

	public static final String HOME_DIR_PROPERTY = "liferay.workspace.home.dir";

	public static final String LIFERAY_PORTAL_URL = "https://releases-cdn.liferay.com/portal/";

	public static final String[] LIFERAY_VERSIONS = {"7.4", "7.3", "7.2", "7.1", "7.0"};

	public static final String MAVEN_HOME_DIR_PROPERTY = "liferayHome";

	public static final String MODULES_DIR_DEFAULT = "modules";

	public static final String MODULES_DIR_PROPERTY = "liferay.workspace.modules.dir";

	public static final String PLUGINS_SDK_DIR_DEFAULT = "plugins-sdk";

	public static final String PLUGINS_SDK_DIR_PROPERTY = "liferay.workspace.plugins.sdk.dir";

	public static final String TARGET_PLATFORM_INDEX_SOURCES_PROPERTY = "target.platform.index.sources";

	public static final String TARGET_PLATFORM_VERSION_PROPERTY = "liferay.workspace.target.platform.version";

	public static final String THEMES_DIR_DEFAULT = "themes";

	public static final String THEMES_DIR_PROPERTY = "liferay.workspace.themes.dir";

	public static final String WARS_DIR_DEFAULT = "wars";

	public static final String WARS_DIR_PROPERTY = "liferay.workspace.wars.dir";

	public static final String WIZARD_LIFERAY_VERSION_FIELD = "selected.liferay.version";

	public static final String WORKSPACE_BOM_VERSION = "liferay.bom.version";

	public static final String WORKSPACE_PRODUCT_PROPERTY = "liferay.workspace.product";

	public static final Map<String, String> liferayBundleUrlVersions = new HashMap<String, String>() {
		{
			put("7.0.6-2", LIFERAY_PORTAL_URL + "7.0.6-ga7/liferay-ce-portal-tomcat-7.0-ga7-20180507111753223.zip");
			put("7.1.0", LIFERAY_PORTAL_URL + "7.1.0-ga1/liferay-ce-portal-tomcat-7.1.0-ga1-20180703012531655.zip");
			put("7.1.1", LIFERAY_PORTAL_URL + "7.1.1-ga2/liferay-ce-portal-tomcat-7.1.1-ga2-20181112144637000.tar.gz");
			put("7.1.2", LIFERAY_PORTAL_URL + "7.1.2-ga3/liferay-ce-portal-tomcat-7.1.2-ga3-20190107144105508.tar.gz");
			put(
				"7.1.3-1",
				LIFERAY_PORTAL_URL + "7.1.3-ga4/liferay-ce-portal-tomcat-7.1.3-ga4-20190508171117552.tar.gz");
			put("7.2.0", LIFERAY_PORTAL_URL + "7.2.0-ga1/liferay-ce-portal-tomcat-7.2.0-ga1-20190531153709761.tar.gz");
			put(
				"7.2.1-1",
				LIFERAY_PORTAL_URL + "7.2.1-ga2/liferay-ce-portal-tomcat-7.2.1-ga2-20191111141448326.tar.gz");
			put(
				"7.3.0-1",
				LIFERAY_PORTAL_URL + "7.3.0-ga1/liferay-ce-portal-tomcat-7.3.0-ga1-20200127150653953.tar.gz");
			put(
				"7.3.1-1",
				LIFERAY_PORTAL_URL + "7.3.1-ga2/liferay-ce-portal-tomcat-7.3.1-ga2-20200327090859603.tar.gz");
			put(
				"7.3.2-1",
				LIFERAY_PORTAL_URL + "7.3.2-ga3/liferay-ce-portal-tomcat-7.3.2-ga3-20200519164024819.tar.gz");
			put(
				"7.3.3-1",
				LIFERAY_PORTAL_URL + "7.3.3-ga4/liferay-ce-portal-tomcat-7.3.3-ga4-20200701015330959.tar.gz");
			put("7.3.4", LIFERAY_PORTAL_URL + "7.3.4-ga5/liferay-ce-portal-tomcat-7.3.4-ga5-20200811154319029.tar.gz");
			put("7.3.5", LIFERAY_PORTAL_URL + "7.3.5-ga6/liferay-ce-portal-tomcat-7.3.5-ga6-20200930172312275.tar.gz");
			put("7.3.6", LIFERAY_PORTAL_URL + "7.3.6-ga7/liferay-ce-portal-tomcat-7.3.6-ga7-20210301155526191.tar.gz");
			put("7.4.0", LIFERAY_PORTAL_URL + "7.4.0-ga1/liferay-ce-portal-tomcat-7.4.0-ga1-20210419204607406.tar.gz");
		}
	};
	public static final Map<String, String[]> targetPlatformVersionMap = new HashMap<String, String[]>() {
		{
			put("7.0", new String[] {"7.0.6-2"});
			put("7.1", new String[] {"7.1.3-1", "7.1.2", "7.1.1", "7.1.0"});
			put("7.2", new String[] {"7.2.1-1", "7.2.0"});
			put("7.3", new String[] {"7.3.6", "7.3.5", "7.3.4", "7.3.3-1", "7.3.2-1", "7.3.1-1", "7.3.0-1"});
			put("7.4", new String[] {"7.4.0"});
		}
	};

}