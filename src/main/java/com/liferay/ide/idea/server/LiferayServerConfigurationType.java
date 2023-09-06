/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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
 * @author Terry Jia
 */
public class LiferayServerConfigurationType extends ConfigurationTypeBase implements ConfigurationType {

	public static String id = "LiferayServerConfiguration";

	public LiferayServerConfigurationType() {
		super(id, "Liferay Server", "Run or Debug a Liferay Server", LiferayIcons.LIFERAY_ICON);

		addFactory(
			new ConfigurationFactory(this) {

				@NotNull
				@Override
				public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
					return new LiferayServerConfiguration(project, this, "");
				}

				@NotNull
				@Override
				public String getId() {
					return super.getName();
				}

			});
	}

}