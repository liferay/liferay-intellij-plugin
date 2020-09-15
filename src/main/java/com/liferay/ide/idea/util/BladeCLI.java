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

import com.intellij.openapi.project.ProjectManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.net.JarURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.JarEntry;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;

/**
 * @author Terry Jia
 */
public class BladeCLI {

	public static final String BLADE_392 = "blade-3.9.2.jar";

	public static final String BLADE_LATEST = "blade-latest.jar";

	public static String[] _execute(File bladeJar, String args) {
		Project project = new Project();
		Java javaTask = new Java();

		javaTask.setProject(project);
		javaTask.setFork(true);
		javaTask.setFailonerror(true);

		javaTask.setJar(bladeJar);

		javaTask.setArgs(args);

		DefaultLogger logger = new DefaultLogger();

		project.addBuildListener(logger);

		StringBufferOutputStream out = new StringBufferOutputStream();

		logger.setOutputPrintStream(new PrintStream(out));

		logger.setMessageOutputLevel(Project.MSG_INFO);

		javaTask.executeJava();

		List<String> lines = new ArrayList<>();
		Scanner scanner = new Scanner(out.toString());

		while (scanner.hasNextLine()) {
			String nextLine = scanner.nextLine();

			lines.add(nextLine.replaceAll(".*\\[null\\] ", ""));
		}

		scanner.close();

		boolean hasErrors = false;

		StringBuilder errors = new StringBuilder();

		for (String line : lines) {
			String lineLowerCase = line.toLowerCase();

			if (lineLowerCase.startsWith("error")) {
				hasErrors = true;
			}
			else if (hasErrors) {
				errors.append(line);
			}
		}

		return lines.toArray(new String[0]);
	}

	public static String[] execute(String args) {
		return _execute(getBladeCLIFile(), args);
	}

	public static String[] executeWithLatestBlade(String args) {
		return _execute(getBladeJar(BLADE_LATEST), args);
	}

	public static synchronized File getBladeCLIFile() {
		ProjectManager projectManager = ProjectManager.getInstance();

		com.intellij.openapi.project.Project workspaceProject = projectManager.getOpenProjects()[0];

		if (Objects.nonNull(workspaceProject)) {
			if (LiferayWorkspaceSupport.isFlexibleLiferayWorkspace(workspaceProject)) {
				_bladeJarName = BLADE_LATEST;
			}
			else {
				_bladeJarName = BLADE_392;
			}
		}
		else {
			_bladeJarName = BLADE_LATEST;
		}

		return getBladeJar(_bladeJarName);
	}

	public static File getBladeJar(String jarName) {
		Properties properties = System.getProperties();

		File temp = new File(properties.getProperty("user.home"), ".liferay-intellij-plugin");

		File bladeJar = new File(temp, jarName);

		boolean needToCopy = true;

		ClassLoader bladeClassLoader = BladeCLI.class.getClassLoader();

		URL url = bladeClassLoader.getResource("/libs/" + jarName);

		try (InputStream in = bladeClassLoader.getResourceAsStream("/libs/" + jarName)) {
			JarURLConnection jarURLConnection = (JarURLConnection)url.openConnection();

			JarEntry jarEntry = jarURLConnection.getJarEntry();

			Long bladeJarTimestamp = jarEntry.getTime();

			if (bladeJar.exists()) {
				Long destTimestamp = bladeJar.lastModified();

				if (destTimestamp < bladeJarTimestamp) {
					bladeJar.delete();
				}
				else {
					needToCopy = false;
				}
			}

			if (needToCopy) {
				FileUtil.writeFile(bladeJar, in);
				bladeJar.setLastModified(bladeJarTimestamp);
			}
		}
		catch (IOException ioe) {
		}

		return bladeJar;
	}

	public static synchronized String[] getProjectTemplates() {
		List<String> templateNames = new ArrayList<>();

		String[] executeResult = execute("create -l");

		for (String name : executeResult) {
			String trimmedName = name.trim();

			if (trimmedName.indexOf(" ") != -1) {
				templateNames.add(name.substring(0, name.indexOf(" ")));
			}
			else {
				templateNames.add(name);
			}
		}

		return templateNames.toArray(new String[0]);
	}

	public static synchronized String[] getWorkspaceProducts(boolean showAll) {
		List<String> workspaceProducts = new ArrayList<>();

		String[] executeResult;

		if (showAll) {
			executeResult = executeWithLatestBlade("init --list --all");
		}
		else {
			executeResult = executeWithLatestBlade("init --list");
		}

		for (String result : executeResult) {
			String category = result.trim();

			if (category.indexOf(" ") == -1) {
				workspaceProducts.add(category);
			}
		}

		return workspaceProducts.toArray(new String[0]);
	}

	private static String _bladeJarName = null;

}