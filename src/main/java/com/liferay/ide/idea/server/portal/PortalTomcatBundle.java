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

import com.intellij.execution.Platform;

import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.PathsUtil;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Simon Jiang
 */
public class PortalTomcatBundle extends AbstractPortalBundle {

	public PortalTomcatBundle(Path path) {
		super(path);
	}

	@Override
	public String getMainClass() {
		return "org.apache.catalina.startup.Bootstrap";
	}

	@Override
	public Path[] getRuntimeClasspath() {
		List<Path> paths = new ArrayList<>();

		Path binPath = PathsUtil.append(bundlePath, "bin");

		if (FileUtil.exist(binPath)) {
			paths.add(PathsUtil.append(binPath, "bootstrap.jar"));

			Path juli = PathsUtil.append(binPath, "tomcat-juli.jar");

			if (FileUtil.exist(juli)) {
				paths.add(juli);
			}

			Path dameonPath = PathsUtil.append(binPath, "commons-daemon.jar");

			if (FileUtil.exist(dameonPath)) {
				paths.add(dameonPath);
			}
		}

		return paths.toArray(new Path[0]);
	}

	@Override
	public String[] getRuntimeStartProgArgs() {
		String[] retval = new String[1];

		retval[0] = "start";

		return retval;
	}

	@Override
	public String[] getRuntimeStartVMArgs() {
		return _getRuntimeVMArgs();
	}

	@Override
	public String getType() {
		return "tomcat";
	}

	@Override
	protected int getDefaultJMXRemotePort() {
		int retval = 8099;

		Path setenv = PathsUtil.append(bundlePath, "bin/setenv." + _getShellExtension());

		String contents = FileUtil.readContents(setenv.toFile(), true);

		String port = null;

		if (contents != null) {
			_pattern = Pattern.compile(".*-Dcom.sun.management.jmxremote.port(\\s*)=(\\s*)([0-9]+).*");

			_matcher = _pattern.matcher(contents);

			if (_matcher.matches()) {
				port = _matcher.group(3);
			}
		}

		if (port != null) {
			try {
				retval = Integer.parseInt(port);
			}
			catch (NumberFormatException nfe) {
			}
		}

		return retval;
	}

	private String[] _getRuntimeVMArgs() {
		List<String> args = new ArrayList<>();
		Path tempPath = PathsUtil.append(bundlePath, "temp");
		Path endorsedPath = PathsUtil.append(bundlePath, "endorsed");

		args.add("-Dcatalina.base=" + bundlePath);
		args.add("-Dcatalina.home=" + bundlePath);
		args.add("-Dcom.sun.management.jmxremote");
		args.add("-Dcom.sun.management.jmxremote.authenticate=false");
		args.add("-Dcom.sun.management.jmxremote.port=" + getJmxRemotePort());
		args.add("-Dcom.sun.management.jmxremote.ssl=false");
		args.add("-Dfile.encoding=UTF8");
		args.add("-Djava.endorsed.dirs=" + endorsedPath);
		args.add("-Djava.io.tmpdir=" + tempPath);
		args.add("-Djava.net.preferIPv4Stack=true");
		args.add("-Djava.util.logging.config.file=" + PathsUtil.append(bundlePath, "conf/logging.properties"));
		args.add("-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager");
		args.add("-Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false");
		args.add("-Duser.timezone=GMT");

		return args.toArray(new String[0]);
	}

	private String _getShellExtension() {
		if (Platform.WINDOWS.equals(Platform.current())) {
			return "bat";
		}

		return "sh";
	}

	private Matcher _matcher;
	private Pattern _pattern;

}