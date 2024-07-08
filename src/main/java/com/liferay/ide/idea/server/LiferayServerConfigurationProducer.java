/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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
public class LiferayServerConfigurationProducer extends LazyRunConfigurationProducer<LiferayServerConfiguration> {

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