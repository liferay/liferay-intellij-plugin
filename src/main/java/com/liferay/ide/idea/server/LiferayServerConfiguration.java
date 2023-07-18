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
import com.intellij.execution.RunManager;
import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.execution.configurations.SearchScopeProvidingRunProfile;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.execution.util.ProgramParametersUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.Strings;
import com.intellij.psi.search.ExecutionSearchScopes;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import com.liferay.ide.idea.core.LiferayCore;
import com.liferay.ide.idea.server.portal.PortalBundle;
import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ServerUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.jdom.Element;
import org.jdom.Namespace;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 * @author Simon Jiang
 * @author Seiphon Wang
 */
@SuppressWarnings("unchecked")
public class LiferayServerConfiguration
	extends LocatableConfigurationBase
	implements CommonJavaRunConfigurationParameters, LiferayWorkspaceSupport, SearchScopeProvidingRunProfile {

	public LiferayServerConfiguration(Project project, ConfigurationFactory factory, String name) {
		super(project, factory, name);

		_javaRunConfigurationModule = new JavaRunConfigurationModule(project, true);

		_vmParameters = "-Xmx2560m -XX:MaxMetaspaceSize=768m -XX:MetaspaceSize=768m";

		_gogoShellPort = ServerUtil.getGogoShellPort(_bundleLocation);

		_javaRunConfigurationModule.setModuleToAnyFirstIfNotSpecified();
	}

	@Override
	public void checkConfiguration() throws RuntimeConfigurationException {
		JavaParametersUtil.checkAlternativeJRE(this);

		ProgramParametersUtil.checkWorkingDirectoryExist(this, getProject(), null);

		if (CoreUtil.isNullOrEmpty(_bundleLocation)) {
			throw new RuntimeConfigurationException("Please set correct bundle location", "Invalid bundle location");
		}

		if (CoreUtil.isNullOrEmpty(_buildType)) {
			throw new RuntimeConfigurationException("Please check bundle location", "Invalid bundle type");
		}

		if (!_isCorrectGogoShellPort(_gogoShellPort)) {
			throw new RuntimeConfigurationWarning(
				"The customized gogo-shell port is not equals defined value in portal-ext.properties, " +
					"portal-developer.properties or portal-setup-wizard.properties");
		}

		JavaRunConfigurationExtensionManager.checkConfigurationIsValid(this);
	}

	@Override
	public RunConfiguration clone() {
		LiferayServerConfiguration clone = (LiferayServerConfiguration)super.clone();

		JavaRunConfigurationModule configurationModule = new JavaRunConfigurationModule(getProject(), true);

		configurationModule.setModule(_javaRunConfigurationModule.getModule());

		clone.setConfigurationModule(configurationModule);

		clone.setEnvs(new LinkedHashMap<>());

		clone.setGogoShellPort(_gogoShellPort);

		String moduleSdkPathString = _getModuleSdkPath();

		if (Objects.nonNull(moduleSdkPathString)) {
			clone.setAlternativeJrePath(moduleSdkPathString);
		}

		Project project = getProject();

		if (Objects.nonNull(LiferayCore.getWorkspaceProvider(project))) {
			String homeDir = getHomeDir(project);

			if (Objects.isNull(homeDir)) {
				return clone;
			}

			Path bundlePath = Paths.get(homeDir);

			if (bundlePath.isAbsolute()) {
				clone.setBundleLocation(homeDir);
			}
			else {
				bundlePath = Paths.get(project.getBasePath(), homeDir);

				clone.setBundleLocation(bundlePath.toString());
			}

			PortalBundle portalBundle = ServerUtil.getPortalBundle(bundlePath);

			if (portalBundle != null) {
				clone.setBundleType(portalBundle.getType());
			}
		}

		return clone;
	}

	@Nullable
	@Override
	public String getAlternativeJrePath() {
		return _alternativeJrePath;
	}

	public String getBundleLocation() {
		return _bundleLocation;
	}

	public String getBundleType() {
		return _buildType;
	}

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
		SettingsEditorGroup<LiferayServerConfiguration> group = new SettingsEditorGroup<>();

		String configurationTitle = ExecutionBundle.message("run.configuration.configuration.tab.title");

		group.addEditor(configurationTitle, new LiferayServerConfigurable(getProject()));

		JavaRunConfigurationExtensionManager javaRunConfigurationExtensionManager =
			JavaRunConfigurationExtensionManager.getInstance();

		javaRunConfigurationExtensionManager.appendEditors(this, group);

		group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel<>());

		return group;
	}

	public boolean getDeveloperMode() {
		return _developerMode;
	}

	@NotNull
	@Override
	public Map<String, String> getEnvs() {
		return _userEnv;
	}

	public String getGogoShellPort() {
		return _gogoShellPort;
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
		return ExecutionSearchScopes.executionScope(Arrays.asList(getModules()));
	}

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
		return new LiferayServerCommandLineState(environment, this);
	}

	@Override
	public String getVMParameters() {
		return _vmParameters;
	}

	@Nullable
	@Override
	public String getWorkingDirectory() {
		return null;
	}

	@Override
	public boolean isAlternativeJrePathEnabled() {
		return _alternativeJrePathEnabled;
	}

	@Override
	public boolean isPassParentEnvs() {
		return _passParentEnvironments;
	}

	@Override
	public void onNewConfigurationCreated() {
		if (StringUtil.isEmpty(getWorkingDirectory())) {
			String baseDir = FileUtil.toSystemIndependentName(StringUtil.notNullize(getProject().getBasePath()));

			setWorkingDirectory(baseDir);
		}

		Project project = getProject();

		RunManager runManager = RunManager.getInstance(project);

		MessageBus messageBus = project.getMessageBus();

		MessageBusConnection messageBusConnection = messageBus.connect(project);

		messageBusConnection.subscribe(
			RunManagerListener.TOPIC,
			new RunManagerListener() {

				public void runConfigurationRemoved(@NotNull RunnerAndConfigurationSettings settings) {
					runManager.removeConfiguration(settings);
					runManager.makeStable(settings);
				}

			});
	}

	@Override
	public void readExternal(Element element) throws InvalidDataException {
		super.readExternal(element);

		Element configurationElement = element.getChild(getName());

		if (configurationElement != null) {
			_bundleLocation = configurationElement.getAttributeValue("bundleLocation");
			_buildType = configurationElement.getAttributeValue("bundleType");
			_vmParameters = configurationElement.getAttributeValue("vmParameters");
			_gogoShellPort = configurationElement.getAttributeValue("gogoShellPort");
			_developerMode = Boolean.parseBoolean(configurationElement.getAttributeValue("developerMode"));
			_alternativeJrePath = configurationElement.getAttributeValue("alternativeJrePath");
			EnvironmentVariablesComponent.readExternal(configurationElement, _userEnv);
			_javaRunConfigurationModule.readExternal(configurationElement);
		}
	}

	@Override
	public void setAlternativeJrePath(String path) {
		_alternativeJrePath = path;
	}

	@Override
	public void setAlternativeJrePathEnabled(boolean enabled) {
		_alternativeJrePathEnabled = enabled;
	}

	public void setBundleLocation(String bundleLocation) {
		_bundleLocation = bundleLocation;
	}

	public void setBundleType(String bundleType) {
		_buildType = bundleType;
	}

	public void setConfigurationModule(JavaRunConfigurationModule configurationModule) {
		_javaRunConfigurationModule = configurationModule;
	}

	public void setDeveloperMode(boolean developerMode) {
		_developerMode = developerMode;
	}

	@Override
	public void setEnvs(@NotNull Map<String, String> myEnv) {
		_userEnv.putAll(myEnv);

		String moduleSdkPath = _getModuleSdkPath();

		if (moduleSdkPath != null) {
			_userEnv.put("JAVA_HOME", moduleSdkPath);

			Map<String, String> systemEnv = System.getenv();

			_userEnv.put("PATH", moduleSdkPath + "/bin:" + systemEnv.get("PATH"));
		}
	}

	public void setGogoShellPort(String gogoShellPort) {
		_gogoShellPort = gogoShellPort;
	}

	public void setModule(Module module) {
		_javaRunConfigurationModule.setModule(module);
	}

	@Override
	public void setPassParentEnvs(boolean passParentEnvs) {
		_passParentEnvironments = passParentEnvs;
	}

	@Override
	public void setProgramParameters(@Nullable String value) {
	}

	@Override
	public void setVMParameters(String value) {
		_vmParameters = value;
	}

	@Override
	public void setWorkingDirectory(@Nullable String value) {
	}

	@Override
	public void writeExternal(Element element) throws WriteExternalException {
		String configugrationName = getName();

		if (Strings.isEmpty(configugrationName)) {
			return;
		}

		Element configurationElement = element.getChild(configugrationName, Namespace.NO_NAMESPACE);

		if (configurationElement == null) {
			configurationElement = new Element(configugrationName, Namespace.NO_NAMESPACE);
		}

		configurationElement.setAttribute("alternativeJrePath", _alternativeJrePath);
		configurationElement.setAttribute("bundleLocation", _bundleLocation);
		configurationElement.setAttribute("bundleType", _buildType);
		configurationElement.setAttribute("developerMode", Boolean.toString(_developerMode));
		configurationElement.setAttribute("gogoShellPort", _gogoShellPort);
		configurationElement.setAttribute("vmParameters", _vmParameters);

		EnvironmentVariablesComponent.writeExternal(configurationElement, getEnvs());

		if (_javaRunConfigurationModule.getModule() != null) {
			_javaRunConfigurationModule.writeExternal(configurationElement);
		}

		element.addContent(configurationElement);

		super.writeExternal(element);
	}

	private String _getModuleSdkPath() {
		Module module = getModule();

		if (module != null) {
			ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(getModule());

			Sdk moduleSdk = moduleRootManager.getSdk();

			if (moduleSdk == null) {
				ProjectRootManager projectRootInstance = ProjectRootManager.getInstance(getProject());

				moduleSdk = projectRootInstance.getProjectSdk();
			}

			if (moduleSdk != null) {
				return moduleSdk.getHomePath();
			}
		}

		return null;
	}

	private boolean _isCorrectGogoShellPort(String gogoShellPort) {
		String extGogoShellPort = ServerUtil.getGogoShellPort(_bundleLocation);

		if (Objects.equals(gogoShellPort, extGogoShellPort)) {
			return true;
		}

		return false;
	}

	private String _alternativeJrePath = "";
	private boolean _alternativeJrePathEnabled = true;
	private String _buildType = "";
	private String _bundleLocation = "";
	private boolean _developerMode = true;
	private String _gogoShellPort = "";
	private JavaRunConfigurationModule _javaRunConfigurationModule;
	private boolean _passParentEnvironments = true;
	private Map<String, String> _userEnv = new HashMap<>();
	private String _vmParameters = "";

}