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
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;

import org.jetbrains.annotations.NonNls;
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

			@NonNls
			@NotNull
			@Override
			public String getId() {
				return super.getId();
			}

		};
	}

	@Override
	public boolean isConfigurationFromContext(
		@NotNull LiferayDockerServerConfiguration configuration, @NotNull ConfigurationContext context) {

		return false;
	}

	@Override
	protected boolean setupConfigurationFromContext(
		@NotNull LiferayDockerServerConfiguration configuration, @NotNull ConfigurationContext context,
		@NotNull Ref<PsiElement> sourceElement) {

		return false;
	}

}