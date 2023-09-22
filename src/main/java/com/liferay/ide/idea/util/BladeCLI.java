/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.net.HttpConfigurable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.nio.file.Files;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;

import org.osgi.framework.Version;

/**
 * @author Terry Jia
 */
public class BladeCLI {

	public static final String BLADE_392 = "blade-3.9.2.jar";

	public static final String BLADE_LATEST = "blade-latest.jar";

	public static String[] execute(File bladeJar, String args) {
		Project project = new Project();
		Java javaTask = new Java();

		javaTask.setProject(project);
		javaTask.setFork(true);
		javaTask.setFailonerror(true);

		javaTask.setJar(bladeJar);

		javaTask.setArgs(args);

		HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();

		if (httpConfigurable.USE_HTTP_PROXY) {
			String[] proxyTypes = {"http", "https"};

			for (String proxyType : proxyTypes) {
				Environment.Variable proxyHostVariable = new Environment.Variable();

				proxyHostVariable.setKey(proxyType + ".proxyHost");
				proxyHostVariable.setValue(httpConfigurable.PROXY_HOST);

				javaTask.addSysproperty(proxyHostVariable);

				Environment.Variable proxyPortVariable = new Environment.Variable();

				proxyPortVariable.setKey(proxyType + ".proxyPort");
				proxyPortVariable.setValue(String.valueOf(httpConfigurable.PROXY_PORT));

				javaTask.addSysproperty(proxyPortVariable);

				if (!httpConfigurable.PROXY_AUTHENTICATION) {
					continue;
				}

				String userId = httpConfigurable.getProxyLogin();
				String userPassword = httpConfigurable.getPlainProxyPassword();

				if (Objects.isNull(userId) || Objects.isNull(userPassword)) {
					continue;
				}

				Environment.Variable proxyUserVariable = new Environment.Variable();

				proxyUserVariable.setKey(proxyType + ".proxyUser");
				proxyUserVariable.setValue(userId);

				javaTask.addSysproperty(proxyUserVariable);

				Environment.Variable proxyPasswordVariable = new Environment.Variable();

				proxyPasswordVariable.setKey(proxyType + ".proxyPassword");
				proxyPasswordVariable.setValue(userPassword);

				javaTask.addSysproperty(proxyPasswordVariable);
			}
		}

		DefaultLogger logger = new DefaultLogger();

		project.addBuildListener(logger);

		List<String> lines = new ArrayList<>();

		int returnCode = 0;

		try (StringBufferOutputStream out = new StringBufferOutputStream();
			PrintStream printStream = new PrintStream(out)) {

			logger.setOutputPrintStream(printStream);

			logger.setMessageOutputLevel(Project.MSG_INFO);

			returnCode = javaTask.executeJava();

			try (Scanner scanner = new Scanner(out.toString())) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();

					lines.add(line.replaceAll(".*\\[null\\] ", ""));
				}
			}

			boolean hasErrors = false;

			StringBuilder errors = new StringBuilder();

			for (String line : lines) {
				if (line.startsWith("Error")) {
					hasErrors = true;
				}
				else if (hasErrors) {
					errors.append(line);
				}
			}

			if ((returnCode != 0) || hasErrors) {
				_logger.error(errors.toString());
			}
		}
		catch (IOException ioException) {
			_logger.error(ioException);
		}

		return lines.toArray(new String[0]);
	}

	public static String[] execute(com.intellij.openapi.project.Project liferayProject, String args) {
		return execute(getBladeJar(getBladeJarVersion(liferayProject)), args);
	}

	public static String[] executeWithLatestBlade(String args) {
		return execute(getBladeJar(BLADE_LATEST), args);
	}

	public static File getBladeJar(String jarName) {
		Properties properties = System.getProperties();

		File temp = new File(properties.getProperty("user.home"), ".liferay-intellij-plugin");

		File bladeJar = new File(temp, jarName);

		boolean needToCopy = true;

		ClassLoader bladeClassLoader = BladeCLI.class.getClassLoader();

		try (InputStream inputStream = bladeClassLoader.getResourceAsStream("libs/" + jarName)) {
			if (bladeJar.exists()) {
				Version newBladeVersion = Version.parseVersion(_getBladeVersion(inputStream));

				try (InputStream existedBladeInputStream = Files.newInputStream(bladeJar.toPath())) {
					Version existedBladeVersion = Version.parseVersion(_getBladeVersion(existedBladeInputStream));

					if (newBladeVersion.compareTo(existedBladeVersion) <= 0) {
						needToCopy = false;
					}
				}
			}
		}
		catch (IOException ioException) {
			_logger.error(ioException);
		}

		try (InputStream inputStream = bladeClassLoader.getResourceAsStream("libs/" + jarName)) {
			if (needToCopy) {
				bladeJar.delete();

				FileUtil.writeFile(bladeJar, inputStream);
			}
		}
		catch (IOException ioException) {
			_logger.error(ioException);
		}

		return bladeJar;
	}

	public static synchronized String getBladeJarVersion(com.intellij.openapi.project.Project workspaceProject) {
		String bladeJarName;

		if (Objects.nonNull(workspaceProject)) {
			if (LiferayWorkspaceSupport.isFlexibleLiferayWorkspace(workspaceProject)) {
				bladeJarName = BLADE_LATEST;
			}
			else {
				bladeJarName = BLADE_392;
			}
		}
		else {
			bladeJarName = BLADE_LATEST;
		}

		return bladeJarName;
	}

	public static synchronized String[] getProjectTemplates(com.intellij.openapi.project.Project liferayProject) {
		List<String> templateNames = new ArrayList<>();

		String[] executeResult = execute(liferayProject, "create -l");

		for (String name : executeResult) {
			String trimmedName = name.trim();

			if (trimmedName.contains(" ")) {
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

			if (!category.contains(" ")) {
				workspaceProducts.add(category);
			}
		}

		return workspaceProducts.toArray(new String[0]);
	}

	private static String _getBladeVersion(InputStream inputStream) {
		try (ZipInputStream zipInput = new ZipInputStream(inputStream)) {
			ZipEntry zipEntry;

			do {
				zipEntry = zipInput.getNextEntry();

				if (Objects.nonNull(zipEntry) && Objects.equals(zipEntry.getName(), "META-INF/MANIFEST.MF")) {
					Manifest manifest = new Manifest(zipInput);

					Attributes mainAttributes = manifest.getMainAttributes();

					return mainAttributes.getValue("Bundle-Version");
				}
			}
			while (zipEntry != null);
		}
		catch (Exception exception) {
			_logger.error(exception);
		}

		return null;
	}

	private static Logger _logger = Logger.getInstance(BladeCLI.class);

}