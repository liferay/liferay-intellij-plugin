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

import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import org.jetbrains.annotations.NonNls;
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

			@NonNls
			@NotNull
			@Override
			public String getId() {
				return super.getName();
			}

		};
	}

	@Override
	public boolean isConfigurationFromContext(
		@NotNull LiferayServerConfiguration configuration, @NotNull ConfigurationContext context) {

		return false;
	}

	@Override
	protected boolean setupConfigurationFromContext(
		@NotNull LiferayServerConfiguration configuration, @NotNull ConfigurationContext context,
		@NotNull Ref<PsiElement> sourceElement) {

		return false;
	}

}