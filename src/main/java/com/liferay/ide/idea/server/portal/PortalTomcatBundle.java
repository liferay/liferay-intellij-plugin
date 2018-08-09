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

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;

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

		Path binPath = FileUtil.pathAppend(bundlePath, "bin");

		if (FileUtil.exist(binPath)) {
			paths.add(FileUtil.pathAppend(binPath, "bootstrap.jar"));

			Path juli = FileUtil.pathAppend(binPath, "tomcat-juli.jar");

			if (FileUtil.exist(juli)) {
				paths.add(juli);
			}

			Path dameonPath = FileUtil.pathAppend(binPath, "commons-daemon.jar");

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

	private String[] _getRuntimeVMArgs() {
		List<String> args = new ArrayList<>();
		Path tempPath = FileUtil.pathAppend(bundlePath, "temp");
		Path endorsedPath = FileUtil.pathAppend(bundlePath, "endorsed");

		args.add("-Dcatalina.base=" + bundlePath);
		args.add("-Dcatalina.home=" + bundlePath);
		args.add("-Dfile.encoding=UTF8");
		args.add("-Djava.endorsed.dirs=" + endorsedPath);
		args.add("-Djava.io.tmpdir=" + tempPath);
		args.add("-Djava.net.preferIPv4Stack=true");
		args.add("-Djava.util.logging.config.file=" + FileUtil.pathAppend(bundlePath, "conf/logging.properties"));
		args.add("-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager");
		args.add("-Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false");
		args.add("-Duser.timezone=GMT");

		return args.toArray(new String[0]);
	}

}