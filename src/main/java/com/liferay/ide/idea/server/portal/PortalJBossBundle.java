/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.server.portal;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.lang.JavaVersion;

import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.ListUtil;

import java.io.File;
import java.io.FileFilter;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;

import org.jetbrains.jps.model.java.JdkVersionDetector;

/**
 * @author Seiphon Wang
 */
public class PortalJBossBundle extends AbstractPortalBundle {

	public static final int DEFAULT_JMX_PORT = 2099;

	public PortalJBossBundle(Path path) {
		super(path);
	}

	@Override
	public String getDisplayName() {
		return "JBoss AS";
	}

	public int getJmxRemotePort() {
		return getDefaultJMXRemotePort();
	}

	@Override
	public String getMainClass() {
		return "org.jboss.modules.Main";
	}

	@Override
	public Path[] getRuntimeClasspath() {
		List<Path> paths = new ArrayList<>();

		if (FileUtil.exists(bundlePath)) {
			paths.add(FileUtil.pathAppend(bundlePath, "jboss-modules.jar"));

			Path loggManagerPath = FileUtil.pathAppend(
				bundlePath, "modules/system/layers/base/org/jboss/logmanager/main");

			File loggManagerFile = loggManagerPath.toFile();

			if (FileUtil.exists(loggManagerFile)) {
				File[] libFiles = loggManagerFile.listFiles(
					new FileFilter() {

						@Override
						public boolean accept(File libFile) {
							String libFileName = libFile.getName();

							if (libFile.isFile() && libFileName.endsWith(".jar")) {
								return true;
							}

							return false;
						}

					});

				if (libFiles != null) {
					for (File libFile : libFiles) {
						paths.add(FileUtil.pathAppend(loggManagerPath, libFile.getName()));
					}
				}
			}
		}

		return paths.toArray(new Path[0]);
	}

	@Override
	public String[] getRuntimeStartProgArgs() {
		List<String> args = new ArrayList<>();

		args.add("-mp");

		Path modulesPath = FileUtil.pathAppend(bundlePath, "modules");

		args.add(modulesPath.toString());

		args.add("-jaxpmodule");
		args.add("javax.xml.jaxp-provider");
		args.add("org.jboss.as.standalone");
		args.add("-b");
		args.add("localhost");
		args.add("--server-config=standalone.xml");
		args.add("-Djboss.server.base.dir=" + FileUtil.pathAppend(bundlePath, "standalone"));

		return args.toArray(new String[0]);
	}

	@Override
	public String[] getRuntimeStartVMArgs(Sdk sdk) {
		List<String> args = getDefaultRuntimeStartVMArgs();

		JdkVersionDetector jdkVersionDetector = JdkVersionDetector.getInstance();

		JdkVersionDetector.JdkVersionInfo jdkVersionInfo = jdkVersionDetector.detectJdkVersionInfo(sdk.getHomePath());

		if (jdkVersionInfo != null) {
			JavaVersion jdkVersion = jdkVersionInfo.version;
			JavaVersion jdk8Version = JavaVersion.compose(8);

			if (jdkVersion.compareTo(jdk8Version) <= 0) {
				File jbossLogmanagerJarFile = getJbossLib(bundlePath, "/modules/org/jboss/logmanager/main/");

				if (Objects.nonNull(jbossLogmanagerJarFile)) {
					args.add("-Xbootclasspath/p:" + jbossLogmanagerJarFile.getAbsolutePath());
				}

				File jbossLogmanagerLog4jJarFile = getJbossLib(bundlePath, "/modules/org/jboss/logmanager/log4j/main/");

				if (Objects.nonNull(jbossLogmanagerLog4jJarFile)) {
					args.add("-Xbootclasspath/p:" + jbossLogmanagerLog4jJarFile.getAbsolutePath());
				}

				File jbosslog4jJarFile = getJbossLib(bundlePath, "/modules/org/apache/log4j/main/");

				if (Objects.nonNull(jbosslog4jJarFile)) {
					args.add("-Xbootclasspath/p:" + jbosslog4jJarFile.getAbsolutePath());
				}
			}
		}

		return args.toArray(new String[0]);
	}

	@Override
	public String getType() {
		return "jboss";
	}

	protected int getDefaultJMXRemotePort() {
		return DEFAULT_JMX_PORT;
	}

	protected List<String> getDefaultRuntimeStartVMArgs() {
		List<String> args = new ArrayList<>();

		args.add("-Dcom.sun.management.jmxremote");
		args.add("-Dcom.sun.management.jmxremote.authenticate=false");
		args.add("-Dcom.sun.management.jmxremote.port=" + getJmxRemotePort());
		args.add("-Dcom.sun.management.jmxremote.ssl=false");
		args.add("-Dorg.jboss.resolver.warning=true");
		args.add("-Djava.net.preferIPv4Stack=true");
		args.add("-Dsun.rmi.dgc.client.gcInterval=3600000");
		args.add("-Dsun.rmi.dgc.server.gcInterval=3600000");
		args.add("-Djboss.modules.system.pkgs=org.jboss.byteman");
		args.add("-Djava.awt.headless=true");
		args.add("-Dfile.encoding=UTF8");
		args.add("-server");
		args.add("-Djava.util.logging.manager=org.jboss.logmanager.LogManager");

		args.add("-Djboss.modules.system.pkgs=org.jboss.logmanager");
		args.add("-Dorg.jboss.boot.log.file=" + FileUtil.pathAppend(bundlePath, "standalone/log/boot.log"));
		args.add(
			"-Dlogging.configuration=file:" +
				FileUtil.pathAppend(bundlePath, "standalone/configuration/logging.properties"));
		args.add("-Djboss.home.dir=" + bundlePath);
		args.add("-Djboss.bind.address.management=localhost");
		args.add("-Duser.timezone=GMT");
		args.add("-Djdk.util.zip.disableZip64ExtraFieldValidation=true");

		return args;
	}

	protected File getJbossLib(Path bundlePath, String libPathValue) {
		Path libIPath = FileUtil.pathAppend(bundlePath, libPathValue);

		Collection<File> libJars = FileUtils.listFiles(libIPath.toFile(), new String[] {"jar"}, true);

		File[] jarArray = libJars.toArray(new File[0]);

		if (ListUtil.isNotEmpty(jarArray)) {
			return jarArray[0];
		}

		return null;
	}

}