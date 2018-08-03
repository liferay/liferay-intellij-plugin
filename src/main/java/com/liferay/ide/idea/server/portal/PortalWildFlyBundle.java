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

import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.ListUtil;

import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Simon Jiang
 */
public class PortalWildFlyBundle extends AbstractPortalBundle {

	public static final int DEFAULT_JMX_PORT = 2099;

	public PortalWildFlyBundle(Path path) {
		super(path);
	}

	@Override
	public String getMainClass() {
		return "org.jboss.modules.Main";
	}

	@Override
	public Path[] getRuntimeClasspath() {
		List<Path> paths = new ArrayList<>();

		if (FileUtil.exist(bundlePath)) {
			paths.add(FileUtil.pathAppend(bundlePath, "jboss-modules.jar"));

			File[] libs = _getJarsFromModules(bundlePath.toString(), "org.jboss.logmanager");

			Stream.of(
				libs
			).forEach(
				lib -> paths.add(Paths.get(lib.getAbsolutePath()))
			);
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
	public String[] getRuntimeStartVMArgs() {
		List<String> args = new ArrayList<>();

		args.add("-Dorg.jboss.resolver.warning=true");
		args.add("-Djava.net.preferIPv4Stack=true");
		args.add("-Dsun.rmi.dgc.client.gcInterval=3600000");
		args.add("-Dsun.rmi.dgc.server.gcInterval=3600000");
		args.add("-Djboss.modules.system.pkgs=org.jboss.byteman,org.jboss.logmanager");
		args.add("-Djava.awt.headless=true");
		args.add("-Dfile.encoding=UTF8");

		args.add("-server");
		args.add("-Djava.util.logging.manager=org.jboss.logmanager.LogManager");

		addBootClasspath(bundlePath.toString(), "org.jboss.logmanager", args, "-Xbootclasspath/p:");
		addBootClasspath(bundlePath.toString(), "org.jboss.log4j.logmanager", args, "-Xbootclasspath/p:");

		args.add("-Dorg.jboss.boot.log.file=" + FileUtil.pathAppend(bundlePath, "standalone/log/boot.log"));
		args.add(
			"-Dlogging.configuration=file:" +
				FileUtil.pathAppend(bundlePath, "standalone/configuration/logging.properties"));
		args.add("-Djboss.home.dir=" + bundlePath);
		args.add("-Djboss.bind.address.management=localhost");
		args.add("-Duser.timezone=GMT");
		args.add("-Dorg.jboss.logmanager.nocolor=true");

		return args.toArray(new String[0]);
	}

	@Override
	public String getType() {
		return "wildfly";
	}

	protected void addBootClasspath(String bundleLocation, String moduleId, List<String> args, String prefix) {
		File[] jars = _getJarsFromModules(bundleLocation, moduleId);

		if (ListUtil.isEmpty(jars)) {
			return;
		}

		Stream.of(
			jars
		).forEach(
			jar -> args.add(prefix + jar.getAbsolutePath())
		);
	}

	private File[] _getJarsFromModules(String bundleLocation, String moduleId) {
		Set<File> jarFiles = new HashSet<>();

		File[] layeredRoots = LayeredModulePathFactory.resolveLayeredModulePath(
			new File[] {new File(bundleLocation, "modules")});

		for (File layeredFile : layeredRoots) {
			File[] jars = LayeredModulePathFactory.getFilesForModule(
				layeredFile, moduleId, null, (dir, name) -> name.endsWith("jar"));

			Collections.addAll(jarFiles, jars);
		}

		return jarFiles.toArray(new File[jarFiles.size()]);
	}

}