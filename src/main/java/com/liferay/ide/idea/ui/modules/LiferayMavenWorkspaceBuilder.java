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

package com.liferay.ide.idea.ui.modules;

import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;

import icons.OpenapiIcons;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenImportingSettings;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

/**
 * @author Joye Luo
 * @author Simon Jiang
 */
public class LiferayMavenWorkspaceBuilder extends LiferayWorkspaceBuilder {

	public LiferayMavenWorkspaceBuilder() {
		super(LiferayProjectType.LIFERAY_MAVEN_WORKSPACE);

		addListener(new LiferayMavenWorkspaceBuilderListener());
	}

	@Override
	public Icon getNodeIcon() {
		return OpenapiIcons.RepositoryLibraryLogo;
	}

	@Override
	public void setupRootModel(ModifiableRootModel modifiableRootModel) {
		initWorkspace(modifiableRootModel.getProject());
	}

	private static class LiferayMavenWorkspaceBuilderListener implements ModuleBuilderListener {

		@Override
		public void moduleCreated(@NotNull Module module) {
			MavenProjectsManager mvnManager = MavenProjectsManager.getInstance(module.getProject());

			MavenImportingSettings mavenImportingSettings = mvnManager.getImportingSettings();

			mavenImportingSettings.setImportAutomatically(true);

			mvnManager.forceUpdateAllProjectsOrFindAllAvailablePomFiles();
		}

	}

}