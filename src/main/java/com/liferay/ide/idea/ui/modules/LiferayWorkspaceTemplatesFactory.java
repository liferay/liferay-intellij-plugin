/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.project.Project;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;

import com.liferay.ide.idea.core.LiferayCore;
import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.core.WorkspaceProvider;
import com.liferay.ide.idea.ui.modules.ext.LiferayModuleExtBuilder;
import com.liferay.ide.idea.ui.modules.ext.LiferayModuleExtTemplate;
import com.liferay.ide.idea.ui.modules.springmvcportlet.LiferaySpringMVCPortletTemplate;
import com.liferay.ide.idea.ui.modules.springmvcportlet.SpringMVCPortletModuleBuilder;

import java.util.Objects;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Joye Luo
 * @author Simon Jiang
 */
public class LiferayWorkspaceTemplatesFactory extends ProjectTemplatesFactory {

	@NotNull
	@Override
	public ProjectTemplate[] createTemplates(@Nullable String group, WizardContext context) {
		Project contextProject = context.getProject();

		if (Objects.isNull(contextProject)) {
			return new ProjectTemplate[] {
				new LiferayWorkspaceTemplate(
					"Liferay Gradle Workspace", "Create Liferay Gradle Workspace", new LiferayGradleWorkspaceBuilder()),
				new LiferayWorkspaceTemplate(
					"Liferay Maven Workspace", "Create Liferay Maven Workspace", new LiferayMavenWorkspaceBuilder())
			};
		}

		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(contextProject);

		if (Objects.isNull(workspaceProvider)) {
			return new ProjectTemplate[0];
		}

		if (workspaceProvider.isGradleWorkspace()) {
			return new ProjectTemplate[] {
				new LiferayModuleTemplate("Liferay Modules", "Create Liferay Module", new LiferayModuleBuilder()),
				new LiferayModuleExtTemplate(
					"Liferay Module Ext", "Create Liferay Module Ext", new LiferayModuleExtBuilder()),
				new LiferaySpringMVCPortletTemplate(
					"Liferay Spring MVC Portlet", "Create Liferay Spring MVC Portlet",
					new SpringMVCPortletModuleBuilder())
			};
		}

		return new ProjectTemplate[] {
			new LiferayModuleTemplate("Liferay Modules", "Create Liferay Module", new LiferayModuleBuilder()),
			new LiferaySpringMVCPortletTemplate(
				"Liferay Spring MVC Portlet", "Create Liferay Spring MVC Portlet", new SpringMVCPortletModuleBuilder())
		};
	}

	@Override
	public Icon getGroupIcon(String group) {
		return LiferayIcons.LIFERAY_ICON;
	}

	@NotNull
	@Override
	public String[] getGroups() {
		return new String[] {"Liferay"};
	}

}