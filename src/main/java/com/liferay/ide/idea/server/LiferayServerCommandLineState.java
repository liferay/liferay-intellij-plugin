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

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.application.BaseJavaApplicationCommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.execution.filters.ArgumentFileFilter;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.LocalPtyOptions;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.target.PtyOptions;
import com.intellij.execution.target.TargetEnvironment;
import com.intellij.execution.target.TargetProgressIndicator;
import com.intellij.execution.target.TargetedCommandLine;
import com.intellij.execution.target.TargetedCommandLineBuilder;
import com.intellij.execution.target.local.LocalTargetEnvironment;
import com.intellij.execution.target.local.LocalTargetEnvironmentRequest;
import com.intellij.execution.target.local.LocalTargets;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.projectRoots.JdkUtil;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.EnvironmentRestorer;
import com.intellij.util.PathsList;
import com.intellij.util.containers.CollectionFactory;

import com.liferay.ide.idea.server.portal.PortalBundle;
import com.liferay.ide.idea.server.portal.PortalBundleFactory;
import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.PortalPropertiesConfiguration;
import com.liferay.ide.idea.util.ServerUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

	public class LiferayTargetEnvironment extends LocalTargetEnvironment {

		public LiferayTargetEnvironment(LocalTargetEnvironment localTargetEnvironment) {
			super(localTargetEnvironment.getRequest());
		}

		public LiferayTargetEnvironment(@NotNull LocalTargetEnvironmentRequest request) {
			super(request);
		}

		@NotNull
		@Override
		public LifeayGeneralCommandLine createGeneralCommandLine(@NotNull TargetedCommandLine commandLine)
			throws CantRunException {

			try {
				PtyOptions ptyOption = commandLine.getPtyOptions();

				LocalPtyOptions localPtyOptions =
					(ptyOption != null) ? LocalTargets.toLocalPtyOptions(ptyOption) : null;

				GeneralCommandLine generalCommandLine;

				if (localPtyOptions != null) {
					PtyCommandLine ptyCommandLine = new PtyCommandLine(commandLine.collectCommandsSynchronously());

					ptyCommandLine.withOptions(localPtyOptions);

					generalCommandLine = ptyCommandLine;
				}
				else {
					generalCommandLine = new GeneralCommandLine(commandLine.collectCommandsSynchronously());
				}

				generalCommandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE);

				String inputFilePath = commandLine.getInputFilePath();

				if (inputFilePath != null) {
					generalCommandLine.withInput(new File(inputFilePath));
				}

				generalCommandLine.withCharset(commandLine.getCharset());

				String workingDirectory = commandLine.getWorkingDirectory();

				if (workingDirectory != null) {
					generalCommandLine.withWorkDirectory(workingDirectory);
				}

				generalCommandLine.withEnvironment(commandLine.getEnvironmentVariables());
				generalCommandLine.setRedirectErrorStream(commandLine.isRedirectErrorStream());

				return new LifeayGeneralCommandLine(generalCommandLine);
			}
			catch (ExecutionException executionException) {
				throw new CantRunException(executionException.getMessage(), executionException);
			}
		}

		@Override
		public Process createProcess(@NotNull TargetedCommandLine commandLine, @NotNull ProgressIndicator indicator)
			throws ExecutionException {

			LifeayGeneralCommandLine generalCommandLine = createGeneralCommandLine(commandLine);

			return generalCommandLine.createProcess();
		}

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

		Path appServerPath = bundleFactory.findAppServerPath(Paths.get(bundleLocation));

		if (appServerPath == null) {
			throw new ExecutionException("Liferay bundle location is invalid.  " + bundleLocation);
		}

		final PortalBundle portalBundle = bundleFactory.create(appServerPath);

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

		Stream.of(
			portalBundle.getRuntimeStartVMArgs(
				JavaParametersUtil.createProjectJdk(liferayServerConfiguration.getProject(), jrePath))
		).forEach(
			vmParametersList::add
		);

		setupJavaParameters(javaParameters);

		try {
			_configureDeveloperMode(liferayServerConfiguration);
		}
		catch (Exception exception) {
			throw new ExecutionException(exception);
		}

		return javaParameters;
	}

	@NotNull
	@Override
	protected OSProcessHandler startProcess() throws ExecutionException {

		TargetEnvironment remoteEnvironment = getEnvironment().getPreparedTargetEnvironment(
			this, TargetProgressIndicator.EMPTY);

		LiferayTargetEnvironment liferayRemoteEnvironment = new LiferayTargetEnvironment(
			(LocalTargetEnvironment)remoteEnvironment);

		TargetedCommandLineBuilder targetedCommandLineBuilder = getTargetedCommandLine();

		TargetedCommandLine targetedCommandLine = targetedCommandLineBuilder.build();

		Process process = liferayRemoteEnvironment.createProcess(targetedCommandLine, new EmptyProgressIndicator());

		Map<String, String> content = targetedCommandLineBuilder.getUserData(JdkUtil.COMMAND_LINE_CONTENT);

		if (content != null) {
			content.forEach((key, value) -> addConsoleFilters(new ArgumentFileFilter(key, value)));
		}

		OSProcessHandler handler = new KillableColoredProcessHandler.Silent(
			process, targetedCommandLine.getCommandPresentation(remoteEnvironment), targetedCommandLine.getCharset(),
			targetedCommandLineBuilder.getFilesToDeleteOnTermination());

		ProcessTerminatedListener.attach(handler);

		JavaRunConfigurationExtensionManager javaRunConfigurationExtensionManager =
			JavaRunConfigurationExtensionManager.getInstance();

		javaRunConfigurationExtensionManager.attachExtensionsToProcess(
			getConfiguration(), handler, getRunnerSettings());

		return handler;
	}

	private void _configureDeveloperMode(LiferayServerConfiguration configuration) throws Exception {
		PortalBundleFactory bundleFactory = ServerUtil.getPortalBundleFactory(configuration.getBundleType());

		Path appServerPath = bundleFactory.findAppServerPath(Paths.get(configuration.getBundleLocation()));

		File file = appServerPath.toFile();

		File portalExtPropertiesFile = new File(file.getParentFile(), "portal-ext.properties");

		if (configuration.getDeveloperMode()) {
			if (!portalExtPropertiesFile.exists()) {
				portalExtPropertiesFile.createNewFile();
			}

			PortalPropertiesConfiguration portalPropertiesConfiguration = new PortalPropertiesConfiguration();

			try (InputStream inputStream = Files.newInputStream(portalExtPropertiesFile.toPath())) {
				portalPropertiesConfiguration.load(inputStream);
			}

			String[] properties = portalPropertiesConfiguration.getStringArray("include-and-override");

			boolean existing = false;

			for (String prop : properties) {
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

			FileUtils.write(
				portalExtPropertiesFile, contents.replace("include-and-override=portal-developer.properties", ""),
				Charset.defaultCharset());
		}
	}

	private class LifeayGeneralCommandLine extends GeneralCommandLine {

		public LifeayGeneralCommandLine(@NotNull List<String> command) {
			int size = command.size();

			if (size > 0) {
				setExePath(command.get(0));

				if (size > 1) {
					addParameters(command.subList(1, size));
				}
			}
		}

		public LifeayGeneralCommandLine(String... command) {
			this(Arrays.asList(command));
		}

		protected LifeayGeneralCommandLine(@NotNull GeneralCommandLine original) {
			super(original);

			_myRedirectErrorStream = original.isRedirectErrorStream();
		}

		@Override
		protected void setupEnvironment(@NotNull Map<String, String> environment) {
			environment.clear();

			if (getParentEnvironmentType() != ParentEnvironmentType.NONE) {
				environment.putAll(getParentEnvironment());
			}

			if (SystemInfo.isUnix) {
				File workDirectory = getWorkDirectory();

				if (workDirectory != null) {
					environment.put(
						"PWD",
						com.intellij.openapi.util.io.FileUtil.toSystemDependentName(workDirectory.getAbsolutePath()));
				}
			}

			if (!getEnvironment().isEmpty()) {
				if (SystemInfo.isWindows) {
					Map<String, String> envVars = CollectionFactory.createCaseInsensitiveStringMap();

					envVars.putAll(environment);
					envVars.putAll(getEnvironment());

					environment.clear();
					environment.putAll(envVars);
				}
				else {
					environment.putAll(getEnvironment());
				}
			}

			environment.remove("PATH");

			LiferayServerConfiguration liferayServerConfiguration = getConfiguration();

			environment.put("JAVA_HOME", liferayServerConfiguration.getAlternativeJrePath());

			EnvironmentRestorer.restoreOverriddenVars(environment);
		}

		//
		@NotNull
		@Override
		protected Process startProcess(@NotNull List<String> escapedCommands) throws IOException {
			return _toProcessBuilderInternal(
				escapedCommands
			).start();
		}

		private ProcessBuilder _toProcessBuilderInternal(List<String> escapedCommands) {
			ProcessBuilder builder = new ProcessBuilder(escapedCommands);

			setupEnvironment(builder.environment());

			builder.directory(getWorkDirectory());

			builder.redirectErrorStream(_myRedirectErrorStream);

			if (getInputFile() != null) {
				builder.redirectInput(ProcessBuilder.Redirect.from(getInputFile()));
			}

			return buildProcess(builder);
		}

		private boolean _myRedirectErrorStream = false;

	}

}