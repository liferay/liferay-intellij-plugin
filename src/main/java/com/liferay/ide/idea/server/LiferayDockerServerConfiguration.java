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

import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.SearchScopeProvidingRunProfile;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.ProgramParametersUtil;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunnableState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopes;
import com.intellij.util.xmlb.SkipDefaultsSerializationFilter;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;

import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jdom.Element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Simon Jiang
 */
@SuppressWarnings("unchecked")
public class LiferayDockerServerConfiguration
	extends LocatableConfigurationBase
	implements CommonProgramRunConfigurationParameters, SearchScopeProvidingRunProfile {

	public LiferayDockerServerConfiguration(Project project, ConfigurationFactory factory, String name) {
		super(project, factory, name);

		_project = project;
		_factory = factory;
		_name = name;

		_javaRunConfigurationModule = new JavaRunConfigurationModule(project, true);
	}

	@Override
	public void checkConfiguration() throws RuntimeConfigurationException {
		ProgramParametersUtil.checkWorkingDirectoryExist(this, getProject(), null);

		if (!LiferayWorkspaceSupport.isValidGradleWorkspaceLocation(_liferayDockerServerConfig.workspaceLocation)) {
			throw new RuntimeConfigurationException(
				"Please set correct workspace project location", "Invalid workspace project location");
		}

		if (CoreUtil.isNullOrEmpty(_liferayDockerServerConfig.dockerImageId) ||
			Objects.equals("loading...", _liferayDockerServerConfig.dockerImageId)) {

			throw new RuntimeConfigurationException("Please set correct docker image id", "Invalid docker image id");
		}

		if (CoreUtil.isNullOrEmpty(_liferayDockerServerConfig.dockerContainerId) ||
			Objects.equals("loading...", _liferayDockerServerConfig.dockerContainerId)) {

			throw new RuntimeConfigurationException(
				"Please set correct docker container id", "Invalid docker container id");
		}
	}

	@Override
	public LiferayDockerServerConfiguration clone() {
		LiferayDockerServerConfiguration clone = (LiferayDockerServerConfiguration)super.clone();

		_liferayDockerServerConfig.workspaceLocation = _project.getBasePath();

		clone.setConfig(XmlSerializerUtil.createCopy(_liferayDockerServerConfig));

		JavaRunConfigurationModule configurationModule = new JavaRunConfigurationModule(getProject(), true);

		configurationModule.setModule(_javaRunConfigurationModule.getModule());

		clone.setConfigurationModule(configurationModule);

		clone.setEnvs(new LinkedHashMap<>(clone.getEnvs()));

		return clone;
	}

	@NotNull
	@Override
	@Transient
	public List<BeforeRunTask<?>> getBeforeRunTasks() {
		return Collections.emptyList();
	}

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
		SettingsEditorGroup<LiferayDockerServerConfiguration> group = new SettingsEditorGroup<>();
		String title = ExecutionBundle.message("run.configuration.configuration.tab.title");

		group.addEditor(title, new LiferayDockerServerConfigurationEditor(getProject()));

		JavaRunConfigurationExtensionManager javaRunConfigurationExtensionManager =
			JavaRunConfigurationExtensionManager.getInstance();

		javaRunConfigurationExtensionManager.appendEditors(this, group);

		group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel<>());

		return group;
	}

	public String getDockerContainerId() {
		return _liferayDockerServerConfig.dockerContainerId;
	}

	public String getDockerImageId() {
		return _liferayDockerServerConfig.dockerImageId;
	}

	@NotNull
	@Override
	public Map<String, String> getEnvs() {
		return _envs;
	}

	public Module getModule() {
		return _javaRunConfigurationModule.getModule();
	}

	@NotNull
	public Module[] getModules() {
		Module module = _javaRunConfigurationModule.getModule();

		if (module != null) {
			return new Module[] {module};
		}

		return Module.EMPTY_ARRAY;
	}

	@Nullable
	@Override
	public String getProgramParameters() {
		return null;
	}

	@Nullable
	@Override
	public GlobalSearchScope getSearchScope() {
		return GlobalSearchScopes.executionScope(Arrays.asList(getModules()));
	}

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env)
		throws ExecutionException {

		ExternalSystemTaskExecutionSettings settings = new ExternalSystemTaskExecutionSettings();

		String debugExecutorId = ToolWindowId.DEBUG;

		settings.setExternalProjectPath(_project.getBasePath());
		settings.setExternalSystemIdString(GradleConstants.SYSTEM_ID.toString());
		settings.setScriptParameters(null);

		ExternalSystemRunConfiguration externalSystemRunConfiguration = new ExternalSystemRunConfiguration(
			GradleConstants.SYSTEM_ID, _project, _factory, _name);

		List<String> taskNames = new ArrayList<>();

		taskNames.add("startDockerContainer");
		taskNames.add("logsDockerContainer");

		settings.setTaskNames(taskNames);

		settings.setScriptParameters("-x createDockerContainer");

		ExternalSystemRunnableState runnableState = new ExternalSystemRunnableState(
			settings, getProject(), debugExecutorId.equals(executor.getId()), externalSystemRunConfiguration, env);

		copyUserDataTo(runnableState);

		return runnableState;
	}

	@Nullable
	@Override
	public String getWorkingDirectory() {
		return null;
	}

	@Override
	public boolean isPassParentEnvs() {
		return _liferayDockerServerConfig.passParentEnvironments;
	}

	@Override
	public void onNewConfigurationCreated() {
		super.onNewConfigurationCreated();

		if (StringUtil.isEmpty(getWorkingDirectory())) {
			String baseDir = FileUtil.toSystemIndependentName(StringUtil.notNullize(getProject().getBasePath()));

			setWorkingDirectory(baseDir);
		}
	}

	@Override
	public void readExternal(Element element) throws InvalidDataException {
		super.readExternal(element);

		JavaRunConfigurationExtensionManager javaRunConfigurationExtensionManager =
			JavaRunConfigurationExtensionManager.getInstance();

		javaRunConfigurationExtensionManager.readExternal(this, element);

		XmlSerializer.deserializeInto(_liferayDockerServerConfig, element);
		EnvironmentVariablesComponent.readExternal(element, getEnvs());

		_javaRunConfigurationModule.readExternal(element);
	}

	public void setConfig(LiferayDockerServerConfig config) {
		_liferayDockerServerConfig = config;
	}

	public void setConfigurationModule(JavaRunConfigurationModule configurationModule) {
		_javaRunConfigurationModule = configurationModule;
	}

	public void setDockerContainerId(String dockerContainerId) {
		_liferayDockerServerConfig.dockerContainerId = dockerContainerId;
	}

	public void setDockerImageId(String dockerImageId) {
		_liferayDockerServerConfig.dockerImageId = dockerImageId;
	}

	@Override
	public void setEnvs(@NotNull Map<String, String> envs) {
		_envs.clear();
		_envs.putAll(envs);
	}

	public void setModule(Module module) {
		_javaRunConfigurationModule.setModule(module);
	}

	@Override
	public void setPassParentEnvs(boolean passParentEnvs) {
		_liferayDockerServerConfig.passParentEnvironments = passParentEnvs;
	}

	@Override
	public void setProgramParameters(@Nullable String value) {
	}

	@Override
	public void setWorkingDirectory(@Nullable String value) {
	}

	@Override
	public void writeExternal(Element element) throws WriteExternalException {
		super.writeExternal(element);

		JavaRunConfigurationExtensionManager javaRunConfigurationExtensionManager =
			JavaRunConfigurationExtensionManager.getInstance();

		javaRunConfigurationExtensionManager.writeExternal(this, element);

		XmlSerializer.serializeInto(_liferayDockerServerConfig, element, new SkipDefaultsSerializationFilter());
		EnvironmentVariablesComponent.writeExternal(element, getEnvs());

		if (_javaRunConfigurationModule.getModule() != null) {
			_javaRunConfigurationModule.writeExternal(element);
		}
	}

	private Map<String, String> _envs = new LinkedHashMap<>();
	private ConfigurationFactory _factory;
	private JavaRunConfigurationModule _javaRunConfigurationModule;
	private LiferayDockerServerConfig _liferayDockerServerConfig = new LiferayDockerServerConfig();
	private String _name;
	private Project _project;

	private static class LiferayDockerServerConfig {

		public String dockerContainerId = "";
		public String dockerImageId = "";
		public boolean passParentEnvironments = true;
		public String workspaceLocation = "";

	}

}