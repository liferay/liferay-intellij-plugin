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

import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner;
import org.jetbrains.plugins.terminal.TerminalProjectOptionsProvider;

/**
 * @author Terry Jia
 */
public class GogoShellDirectRunner extends LocalTerminalDirectRunner {

	public GogoShellDirectRunner(Project project) {
		super(project);
	}

	@Override
	protected PtyProcess createProcess(@Nullable String workingDirectory) throws ExecutionException {
		if (workingDirectory == null) {
			TerminalProjectOptionsProvider terminalProjectOptionsProvider =
				TerminalProjectOptionsProvider.Companion.getInstance(getProject());

			workingDirectory = terminalProjectOptionsProvider.getStartingDirectory();
		}

		try {
			String[] command = {"telnet", "localhost", _findPort("11311")};

			return PtyProcess.exec(command, _createEnv(), workingDirectory);
		}
		catch (IOException ioe) {
			throw new ExecutionException(ioe);
		}
	}

	private Map<String, String> _createEnv() {
		Map<String, String> env = new HashMap<>(System.getenv());

		if (!SystemInfo.isWindows) {
			env.put("TERM", "xterm-256color");
		}

		if (SystemInfo.isMac) {
			EnvironmentUtil.setLocaleEnv(env, CharsetToolkit.UTF8_CHARSET);
		}

		return env;
	}

	private String _findPort(String defaultValue) {
		String port = defaultValue;

		final Project project = getProject();

		VirtualFile projectBaseDir = project.getBaseDir();

		VirtualFile bundlesDir = projectBaseDir.findChild(LiferayWorkspaceUtil.getHomeDir(project.getBasePath()));

		if ((bundlesDir != null) && bundlesDir.exists()) {
			VirtualFile portalExtProperties = bundlesDir.findChild("portal-ext.properties");

			if ((portalExtProperties != null) && portalExtProperties.exists()) {
				Properties properties = new Properties();

				try (InputStream in = portalExtProperties.getInputStream()) {
					properties.load(in);

					String value = properties.getProperty("module.framework.properties.osgi.console");

					if (value != null) {
						String[] split = value.split(":");

						if (split.length == 2) {
							port = split[1].trim();
						}
					}
				}
				catch (IOException ioe) {
				}
			}
		}

		return port;
	}

}