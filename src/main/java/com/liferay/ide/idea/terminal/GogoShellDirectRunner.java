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

package com.liferay.ide.idea.terminal;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EnvironmentUtil;

import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

import com.pty4j.PtyProcess;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner;
import org.jetbrains.plugins.terminal.TerminalProjectOptionsProvider;

/**
 * @author Terry Jia
 */
public class GogoShellDirectRunner extends LocalTerminalDirectRunner {

	@NotNull
	public static GogoShellDirectRunner createTerminalRunner(Project project) {
		return new GogoShellDirectRunner(project);
	}

	public GogoShellDirectRunner(Project project) {
		super(project);
	}

	@Override
	protected PtyProcess createProcess(@Nullable String directory) throws ExecutionException {
		Map<String, String> envs = new HashMap<>(System.getenv());

		if (!SystemInfo.isWindows) {
			envs.put("TERM", "xterm-256color");
		}

		if (SystemInfo.isMac) {
			EnvironmentUtil.setLocaleEnv(envs, CharsetToolkit.UTF8_CHARSET);
		}

		VirtualFile root = myProject.getBaseDir();

		String port = "11311";

		VirtualFile bundles = root.findChild(LiferayWorkspaceUtil.getHomeDir(myProject.getBasePath()));

		if ((bundles != null) && bundles.exists()) {
			VirtualFile portalext = bundles.findChild("portal-ext.properties");

			if ((portalext != null) && portalext.exists()) {
				Properties properties = new Properties();

				try (InputStream in = portalext.getInputStream()) {
					properties.load(in);

					String s = properties.getProperty("module.framework.properties.osgi.console");

					if (s != null) {
						String[] r = s.split(":");

						if (r.length == 2) {
							port = r[1];
						}
					}
				}
				catch (IOException ioe) {
				}
			}
		}

		String[] command = {"telnet", "localhost", port};

		if (directory == null) {
			TerminalProjectOptionsProvider provider = TerminalProjectOptionsProvider.Companion.getInstance(myProject);

			directory = provider.getStartingDirectory();
		}

		try {
			return PtyProcess.exec(command, envs, directory);
		}
		catch (IOException ioe) {
			throw new ExecutionException(ioe);
		}
	}

}