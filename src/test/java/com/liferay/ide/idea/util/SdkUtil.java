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

package com.liferay.ide.idea.util;

import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JdkUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.SystemProperties;
import com.intellij.util.lang.JavaVersion;

import java.io.File;

import org.jetbrains.jps.model.java.JdkVersionDetector;

/**
 * @author Simon Jiang
 */
public class SdkUtil {

	public static Sdk getTestJdk() {
		File javaHome = new File(SystemProperties.getJavaHome());

		if (JdkUtil.checkForJre(javaHome.toPath()) && !JdkUtil.checkForJdk(javaHome.toPath())) {

			// handle situation like javaHome="<somewhere>/jdk1.8.0_212/jre" (see IDEA-226353)

			File javaHomeParent = javaHome.getParentFile();

			if ((javaHomeParent != null) && JdkUtil.checkForJre(javaHomeParent.toPath()) &&
				JdkUtil.checkForJdk(javaHomeParent.toPath())) {

				javaHome = javaHomeParent;
			}
		}

		String versionName = JdkVersionDetector.formatVersionString(JavaVersion.current());

		JavaSdk javaSdk = JavaSdk.getInstance();

		return javaSdk.createJdk(versionName, javaHome.getAbsolutePath(), !JdkUtil.checkForJdk(javaHome.toPath()));
	}

}