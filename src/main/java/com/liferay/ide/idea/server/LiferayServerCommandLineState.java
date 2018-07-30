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
import java.io.IOException;
import java.io.InputStream;

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
		@NotNull LiferayServerConfiguration configuration, ExecutionEnvironment environment) {

		super(environment, configuration);
	}

	@Override
	protected JavaParameters createJavaParameters() throws ExecutionException {
		JavaParameters params = new JavaParameters();

		LiferayServerConfiguration liferayServerConfiguration = getConfiguration();

		String jreHome = null;

		if (liferayServerConfiguration.isAlternativeJrePathEnabled()) {
			jreHome = liferayServerConfiguration.getAlternativeJrePath();
		}

		params.setJdk(JavaParametersUtil.createProjectJdk(liferayServerConfiguration.getProject(), jreHome));

		String bundleLocation = liferayServerConfiguration.getBundleLocation();

		String bundleType = liferayServerConfiguration.getBundleType();

		PortalBundleFactory bundleFactory = ServerUtil.getPortalBundleFactory(bundleType);

		Path bundlePath = bundleFactory.canCreateFromPath(Paths.get(bundleLocation));

		if (bundlePath == null) {
			throw new ExecutionException("Liferay portal bundle can't be set null");
		}

		final PortalBundle bundle = bundleFactory.create(bundlePath);

		ParametersList programParametersList = params.getProgramParametersList();

		String[] runtimeStartProgArgs = bundle.getRuntimeStartProgArgs();

		Stream.of(
			runtimeStartProgArgs
		).forEach(
			startProg -> programParametersList.add(startProg)
		);

		PathsList classPath = params.getClassPath();
		Path[] runtimeClasspath = bundle.getRuntimeClasspath();

		Stream.of(
			runtimeClasspath
		).forEach(
			path -> classPath.add(path.toFile())
		);

		params.setMainClass(bundle.getMainClass());

		ParametersList vmParametersList = params.getVMParametersList();

		String[] runtimeStartVMArgs = bundle.getRuntimeStartVMArgs();

		Stream.of(
			runtimeStartVMArgs
		).forEach(
			vmArg -> vmParametersList.add(vmArg)
		);

		setupJavaParameters(params);

		_configureDeveloperMode(liferayServerConfiguration);

		return params;
	}

	private void _configureDeveloperMode(LiferayServerConfiguration configuration) {
		String bundleLocation = configuration.getBundleLocation();

		File portalExt = new File(bundleLocation, "portal-ext.properties");

		if (configuration.getDeveloperMode()) {
			try {
				if (!portalExt.exists()) {
					portalExt.createNewFile();
				}

				PortalPropertiesConfiguration config = new PortalPropertiesConfiguration();

				try (InputStream in = Files.newInputStream(portalExt.toPath())) {
					config.load(in);
				}

				String[] p = config.getStringArray("include-and-override");

				boolean existing = false;

				for (String prop : p) {
					if (prop.equals("portal-developer.properties")) {
						existing = true;

						break;
					}
				}

				if (!existing) {
					config.addProperty("include-and-override", "portal-developer.properties");
				}

				config.save(portalExt);
			}
			catch (Exception e) {
			}
		}
		else if (portalExt.exists()) {
			String contents = FileUtil.readContents(portalExt, true);

			contents = contents.replace("include-and-override=portal-developer.properties", "");

			try {
				FileUtils.write(portalExt, contents);
			}
			catch (IOException ioe) {
			}
		}
	}

}