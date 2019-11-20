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

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;

import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * @author Simon Jiang
 */
public class LiferayServerConfigurationProducer
	extends LazyRunConfigurationProducer<LiferayServerConfiguration> implements LiferayWorkspaceSupport {

	@NotNull
	@Override
	public ConfigurationFactory getConfigurationFactory() {
		return new ConfigurationFactory(new LiferayServerConfigurationType()) {

			@NotNull
			@Override
			public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
				return new LiferayServerConfiguration(project, this, "Liferay Sever");
			}

		};
	}

	@Override
	public boolean isConfigurationFromContext(
		@NotNull LiferayServerConfiguration configuration, @NotNull ConfigurationContext context) {

		boolean validWorkspaceLocation = LiferayWorkspaceSupport.isValidWorkspaceLocation(context.getProject());

		if (!validWorkspaceLocation) {
			return false;
		}

		Project project = context.getProject();

		String bundleLocation = configuration.getBundleLocation();

		String basePath = project.getBasePath();

		Path bundlePath = Paths.get(basePath, getHomeDir(project));

		if (!Objects.equals(bundleLocation, bundlePath.toString())) {
			return false;
		}

		return true;
	}

	@Override
	protected boolean setupConfigurationFromContext(
		@NotNull LiferayServerConfiguration configuration, @NotNull ConfigurationContext context,
		@NotNull Ref<PsiElement> sourceElement) {

		if (sourceElement.isNull()) {
			return false;
		}

		Module module = ModuleUtilCore.findModuleForPsiElement(sourceElement.get());

		if (module == null) {
			return false;
		}

		boolean validWorkspaceLocation = LiferayWorkspaceSupport.isValidWorkspaceLocation(context.getProject());

		ConfigurationType configurationType = configuration.getType();

		if (!Objects.equals(configurationType.getId(), LiferayServerConfigurationType.id) || !validWorkspaceLocation) {
			return false;
		}

		return true;
	}

}