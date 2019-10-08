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

package com.liferay.ide.idea.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.application.BaseJavaApplicationCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.util.PathsList;

import com.liferay.ide.idea.server.portal.PortalBundle;
import com.liferay.ide.idea.server.portal.PortalBundleFactory;
import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.PortalPropertiesConfiguration;
import com.liferay.ide.idea.util.ServerUtil;

import java.io.File;
import java.io.InputStream;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import org.jetbrains.annotations.NotNull;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class LiferayServerCommandLineState extends BaseJavaApplicationCommandLineState<LiferayServerConfiguration> {

	public LiferayServerCommandLineState(
		ExecutionEnvironment executionEnvironment, @NotNull LiferayServerConfiguration liferayServerConfiguration) {

		super(executionEnvironment, liferayServerConfiguration);
	}

	@Override
	protected JavaParameters createJavaParameters() throws ExecutionException {
		JavaParameters javaParameters = new JavaParameters();

		LiferayServerConfiguration liferayServerConfiguration = getConfiguration();

		String jrePath = null;

		if (liferayServerConfiguration.isAlternativeJrePathEnabled()) {
			jrePath = liferayServerConfiguration.getAlternativeJrePath();
		}

		javaParameters.setJdk(JavaParametersUtil.createProjectJdk(liferayServerConfiguration.getProject(), jrePath));

		String bundleLocation = liferayServerConfiguration.getBundleLocation();

		PortalBundleFactory bundleFactory = ServerUtil.getPortalBundleFactory(
			liferayServerConfiguration.getBundleType());

		Path bundlePath = bundleFactory.canCreateFromPath(Paths.get(bundleLocation));

		if (bundlePath == null) {
			throw new ExecutionException("Liferay bundle location is invalid.  " + bundleLocation);
		}

		final PortalBundle portalBundle = bundleFactory.create(bundlePath);

		ParametersList programParametersList = javaParameters.getProgramParametersList();

		Stream.of(
			portalBundle.getRuntimeStartProgArgs()
		).forEach(
			programParametersList::add
		);

		PathsList classPath = javaParameters.getClassPath();

		Stream.of(
			portalBundle.getRuntimeClasspath()
		).map(
			Path::toFile
		).forEach(
			classPath::add
		);

		javaParameters.setMainClass(portalBundle.getMainClass());

		ParametersList vmParametersList = javaParameters.getVMParametersList();

		String[] runtimeStartVMArgs = portalBundle.getRuntimeStartVMArgs(
			JavaParametersUtil.createProjectJdk(liferayServerConfiguration.getProject(), jrePath));

		Stream.of(
			runtimeStartVMArgs
		).forEach(
			vmParametersList::add
		);

		setupJavaParameters(javaParameters);

		try {
			_configureDeveloperMode(liferayServerConfiguration);
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}

		return javaParameters;
	}

	private void _configureDeveloperMode(LiferayServerConfiguration configuration) throws Exception {
		PortalBundleFactory bundleFactory = ServerUtil.getPortalBundleFactory(configuration.getBundleType());

		Path bundlePath = bundleFactory.canCreateFromPath(Paths.get(configuration.getBundleLocation()));

		File file = bundlePath.toFile();

		File parentFile = file.getParentFile();

		File portalExtPropertiesFile = new File(parentFile, "portal-ext.properties");

		if (configuration.getDeveloperMode()) {
			if (!portalExtPropertiesFile.exists()) {
				portalExtPropertiesFile.createNewFile();
			}

			PortalPropertiesConfiguration portalPropertiesConfiguration = new PortalPropertiesConfiguration();

			try (InputStream in = Files.newInputStream(portalExtPropertiesFile.toPath())) {
				portalPropertiesConfiguration.load(in);
			}

			String[] p = portalPropertiesConfiguration.getStringArray("include-and-override");

			boolean existing = false;

			for (String prop : p) {
				if (prop.equals("portal-developer.properties")) {
					existing = true;

					break;
				}
			}

			if (!existing) {
				portalPropertiesConfiguration.addProperty("include-and-override", "portal-developer.properties");
			}

			portalPropertiesConfiguration.save(portalExtPropertiesFile);
		}
		else if (portalExtPropertiesFile.exists()) {
			String contents = FileUtil.readContents(portalExtPropertiesFile, true);

			contents = contents.replace("include-and-override=portal-developer.properties", "");

			FileUtils.write(portalExtPropertiesFile, contents, Charset.defaultCharset());
		}
	}

}