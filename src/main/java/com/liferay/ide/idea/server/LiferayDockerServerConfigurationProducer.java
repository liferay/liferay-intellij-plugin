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
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;

import com.liferay.blade.gradle.tooling.ProjectInfo;
import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * @author Simon Jiang
 */
public class LiferayDockerServerConfigurationProducer
	extends LazyRunConfigurationProducer<LiferayDockerServerConfiguration> {

	@NotNull
	@Override
	public ConfigurationFactory getConfigurationFactory() {
		return new ConfigurationFactory(new LiferayDockerServerConfigurationType()) {

			@NotNull
			@Override
			public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
				return new LiferayDockerServerConfiguration(project, this, "Liferay Docker");
			}

		};
	}

	@Override
	public boolean isConfigurationFromContext(
		@NotNull LiferayDockerServerConfiguration configuration, @NotNull ConfigurationContext context) {

		Project project = context.getProject();

		boolean validWorkspaceLocation = LiferayWorkspaceSupport.isValidWorkspaceLocation(project);

		if (!validWorkspaceLocation) {
			return false;
		}

		String dockerImageId = configuration.getDockerImageId();

		String dockerContainerId = configuration.getDockerContainerId();

		try {
			ProjectInfo projectInfo = GradleUtil.getModel(ProjectInfo.class, ProjectUtil.guessProjectDir(project));

			if (!dockerContainerId.equals(projectInfo.getDockerContainerId()) ||
				!dockerImageId.equals(projectInfo.getDockerImageId())) {

				return false;
			}
		}
		catch (Exception e) {
			return false;
		}

		return true;
	}

	@Override
	protected boolean setupConfigurationFromContext(
		@NotNull LiferayDockerServerConfiguration configuration, @NotNull ConfigurationContext context,
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

		if (!Objects.equals(configurationType.getId(), LiferayDockerServerConfigurationType.id) ||
			!validWorkspaceLocation) {

			return false;
		}

		return true;
	}

}