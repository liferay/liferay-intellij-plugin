/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;

import icons.OpenapiIcons;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

/**
 * @author Joye Luo
 * @author Simon Jiang
 */
public class LiferayMavenWorkspaceBuilder extends LiferayWorkspaceBuilder {

	public LiferayMavenWorkspaceBuilder() {
		super(LiferayProjectType.LIFERAY_MAVEN_WORKSPACE);

		addListener(
			new ModuleBuilderListener() {

				@Override
				public void moduleCreated(@NotNull Module module) {
					Project mavenProject = module.getProject();

					MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(mavenProject);

					mavenProjectsManager.forceUpdateAllProjectsOrFindAllAvailablePomFiles();

					removeListener(this);
				}

			});
	}

	@Override
	public Icon getNodeIcon() {
		return OpenapiIcons.RepositoryLibraryLogo;
	}

	@Override
	public void setupRootModel(ModifiableRootModel modifiableRootModel) {
		initWorkspace(modifiableRootModel.getProject());
	}

}