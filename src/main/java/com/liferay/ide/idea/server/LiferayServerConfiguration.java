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
import com.intellij.execution.CommonJavaRunConfigurationParameters;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.SearchScopeProvider;
import com.intellij.execution.configurations.SearchScopeProvidingRunProfile;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.execution.util.ProgramParametersUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.xmlb.SkipDefaultValuesSerializationFilters;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;

import com.liferay.ide.idea.util.CoreUtil;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jdom.Element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class LiferayServerConfiguration
	extends LocatableConfigurationBase implements CommonJavaRunConfigurationParameters, SearchScopeProvidingRunProfile {

	public LiferayServerConfiguration(Project project, ConfigurationFactory factory, String name) {
		super(project, factory, name);

		_javaRunConfigurationModule = new JavaRunConfigurationModule(project, true);

		_liferayServerConfig.vmParameters = "-Xmx1024m";
	}

	@Override
	public void checkConfiguration() throws RuntimeConfigurationException {
		JavaParametersUtil.checkAlternativeJRE(this);

		ProgramParametersUtil.checkWorkingDirectoryExist(this, getProject(), null);

		if (CoreUtil.isNullOrEmpty(_liferayServerConfig.bundleLocation)) {
			throw new RuntimeConfigurationException("Please set correct bundle location", "Invalid bundle location");
		}

		if (CoreUtil.isNullOrEmpty(_liferayServerConfig.buildType)) {
			throw new RuntimeConfigurationException("Please check bundle location", "Invalid bundle type");
		}

		JavaRunConfigurationExtensionManager.checkConfigurationIsValid(this);
	}

	@Override
	public RunConfiguration clone() {
		LiferayServerConfiguration clone = (LiferayServerConfiguration)super.clone();

		clone.setConfig(XmlSerializerUtil.createCopy(_liferayServerConfig));

		JavaRunConfigurationModule configurationModule = new JavaRunConfigurationModule(getProject(), true);

		configurationModule.setModule(_javaRunConfigurationModule.getModule());

		clone.setConfigurationModule(configurationModule);

		clone.setEnvs(new LinkedHashMap<>(clone.getEnvs()));

		return clone;
	}

	@Nullable
	@Override
	public String getAlternativeJrePath() {
		return _liferayServerConfig.alternativeJrePath;
	}

	public String getBundleLocation() {
		return _liferayServerConfig.bundleLocation;
	}

	public String getBundleType() {
		return _liferayServerConfig.buildType;
	}

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
		SettingsEditorGroup<LiferayServerConfiguration> group = new SettingsEditorGroup<>();

		String title = ExecutionBundle.message("run.configuration.configuration.tab.title");

		group.addEditor(title, new LiferayServerConfigurable(getProject()));

		JavaRunConfigurationExtensionManager javaRunConfigurationExtensionManager =
			JavaRunConfigurationExtensionManager.getInstance();

		javaRunConfigurationExtensionManager.appendEditors(this, group);

		group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel<>());

		return group;
	}

	public boolean getDeveloperMode() {
		return _liferayServerConfig.developerMode;
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
	public String getPackage() {
		return null;
	}

	@Nullable
	@Override
	public String getProgramParameters() {
		return null;
	}

	@Nullable
	@Override
	public String getRunClass() {
		return null;
	}

	@Nullable
	@Override
	public GlobalSearchScope getSearchScope() {
		return SearchScopeProvider.createSearchScope(getModules());
	}

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
		return new LiferayServerCommandLineState(environment, this);
	}

	@Override
	public String getVMParameters() {
		return _liferayServerConfig.vmParameters;
	}

	@Nullable
	@Override
	public String getWorkingDirectory() {
		return null;
	}

	@Override
	public boolean isAlternativeJrePathEnabled() {
		return _liferayServerConfig.alternativeJrePathEnabled;
	}

	@Override
	public boolean isPassParentEnvs() {
		return _liferayServerConfig.passParentEnvironments;
	}

	@Override
	public void readExternal(Element element) throws InvalidDataException {
		super.readExternal(element);

		JavaRunConfigurationExtensionManager javaRunConfigurationExtensionManager =
			JavaRunConfigurationExtensionManager.getInstance();

		javaRunConfigurationExtensionManager.readExternal(this, element);

		XmlSerializer.deserializeInto(_liferayServerConfig, element);
		EnvironmentVariablesComponent.readExternal(element, getEnvs());

		_javaRunConfigurationModule.readExternal(element);
	}

	@Override
	public void setAlternativeJrePath(String path) {
		_liferayServerConfig.alternativeJrePath = path;
	}

	@Override
	public void setAlternativeJrePathEnabled(boolean enabled) {
		_liferayServerConfig.alternativeJrePathEnabled = enabled;
	}

	public void setBundleLocation(String bundleLocation) {
		_liferayServerConfig.bundleLocation = bundleLocation;
	}

	public void setBundleType(String bundleType) {
		_liferayServerConfig.buildType = bundleType;
	}

	public void setConfig(LiferayServerConfig config) {
		_liferayServerConfig = config;
	}

	public void setConfigurationModule(JavaRunConfigurationModule configurationModule) {
		_javaRunConfigurationModule = configurationModule;
	}

	public void setDeveloperMode(boolean developerMode) {
		_liferayServerConfig.developerMode = developerMode;
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
		_liferayServerConfig.passParentEnvironments = passParentEnvs;
	}

	@Override
	public void setProgramParameters(@Nullable String value) {
	}

	@Override
	public void setVMParameters(String value) {
		_liferayServerConfig.vmParameters = value;
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

		XmlSerializer.serializeInto(_liferayServerConfig, element, new SkipDefaultValuesSerializationFilters());
		EnvironmentVariablesComponent.writeExternal(element, getEnvs());

		if (_javaRunConfigurationModule.getModule() != null) {
			_javaRunConfigurationModule.writeExternal(element);
		}
	}

	private Map<String, String> _envs = new LinkedHashMap<>();
	private JavaRunConfigurationModule _javaRunConfigurationModule;
	private LiferayServerConfig _liferayServerConfig = new LiferayServerConfig();

	private static class LiferayServerConfig {

		public String alternativeJrePath = "";
		public boolean alternativeJrePathEnabled = true;
		public String buildType = "";
		public String bundleLocation = "";
		public boolean developerMode = true;
		public boolean passParentEnvironments = true;
		public String vmParameters = "";

	}

}