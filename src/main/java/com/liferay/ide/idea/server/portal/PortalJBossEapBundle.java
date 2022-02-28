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

package com.liferay.ide.idea.server.portal;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.lang.JavaVersion;

import com.liferay.ide.idea.util.FileUtil;

import java.io.File;

import java.nio.file.Path;

import java.util.List;
import java.util.Objects;

import org.jetbrains.jps.model.java.JdkVersionDetector;

/**
 * @author Seiphon Wang
 */
public class PortalJBossEapBundle extends PortalJBossBundle {

	public PortalJBossEapBundle(Path path) {
		super(path);
	}

	@Override
	public String[] getRuntimeStartVMArgs(Sdk sdk) {
		List<String> args = getDefaultRuntimeStartVMArgs();

		JdkVersionDetector jdkVersionDetector = JdkVersionDetector.getInstance();

		JdkVersionDetector.JdkVersionInfo jdkVersionInfo = jdkVersionDetector.detectJdkVersionInfo(sdk.getHomePath());

		if (jdkVersionInfo != null) {
			JavaVersion jdkVersion = jdkVersionInfo.version;
			JavaVersion jdk9Version = JavaVersion.compose(9);

			File wildflyCommonLib = getJbossLib(bundlePath, "/modules/system/layers/base/org/wildfly/common/main/");

			if (jdkVersion.compareTo(jdk9Version) < 0) {
				File log4jJbossLogmanager = getJbossLib(
						bundlePath, "/modules/system/layers/base/org/jboss/log4j/logmanager/main/");

				if (Objects.nonNull(log4jJbossLogmanager)) {
					args.add("-Xbootclasspath/p:" + log4jJbossLogmanager.getAbsolutePath());
				}

				if (Objects.nonNull(wildflyCommonLib)) {
					args.add("-Xbootclasspath/p:" + wildflyCommonLib.getAbsolutePath());
				}

				File jbossLogManagerLib = getJbossLib(
					bundlePath, "/modules/system/layers/base/org/jboss/logmanager/main/");

				if (Objects.nonNull(jbossLogManagerLib)) {
					args.add("-Xbootclasspath/p:" + jbossLogManagerLib.getAbsolutePath());
				}
			}
			else if (Objects.nonNull(wildflyCommonLib)) {
				args.add("-Xbootclasspath/a:" + wildflyCommonLib.getAbsolutePath());

				args.add("--add-modules=java.se");
			}
		}

		args.add("-Dorg.jboss.logmanager.nocolor=true");

		return args.toArray(new String[0]);
	}

	@Override
	public String getType() {
		return "jboss_eap";
	}

	@Override
	public String getDisplayName() {
		return "JBoss EAP";
	}
}