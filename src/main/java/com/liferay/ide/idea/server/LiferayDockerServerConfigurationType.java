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

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

import com.liferay.ide.idea.core.LiferayIcons;

import org.jetbrains.annotations.NotNull;

/**
 * @author Simon jiang
 */
public class LiferayDockerServerConfigurationType extends ConfigurationTypeBase implements ConfigurationType {

	public static String id = "LiferayDockerServerConfiguration";

	public LiferayDockerServerConfigurationType() {
		super(id, "Liferay Docker Server", "Run or Debug a Liferay Docker Server", LiferayIcons.LIFERAY_ICON);

		addFactory(
			new ConfigurationFactory(this) {

				@NotNull
				@Override
				public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
					LiferayDockerServerConfiguration dockerServerConfiguration = new LiferayDockerServerConfiguration(
						project, this, "Liferay Docker");

					dockerServerConfiguration.setAllowRunningInParallel(false);

					return dockerServerConfiguration;
				}

				@NotNull
				@Override
				public String getId() {
					return super.getName();
				}

			});
	}

}