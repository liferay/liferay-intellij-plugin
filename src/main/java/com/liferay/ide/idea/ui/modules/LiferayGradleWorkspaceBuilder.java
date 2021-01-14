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

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.service.project.manage.ExternalProjectsManager;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;

import icons.GradleIcons;

import javax.swing.Icon;

import org.jetbrains.plugins.gradle.settings.DistributionType;
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings;
import org.jetbrains.plugins.gradle.settings.GradleSettings;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Joye Luo
 * @author Simon Jiang
 */
public class LiferayGradleWorkspaceBuilder extends LiferayWorkspaceBuilder {

	public LiferayGradleWorkspaceBuilder() {
		super(LiferayProjectType.LIFERAY_GRADLE_WORKSPACE);
	}

	@Override
	public Icon getNodeIcon() {
		return GradleIcons.Gradle;
	}

	@Override
	public void setupRootModel(ModifiableRootModel modifiableRootModel) {
		initWorkspace(modifiableRootModel.getProject());
	}

	@Override
	protected void setupModule(Module module) throws ConfigurationException {
		super.setupModule(module);

		Project project = module.getProject();

		Runnable runnable = () -> {
			GradleProjectSettings gradleProjectSettings = new GradleProjectSettings();

			gradleProjectSettings.setExternalProjectPath(project.getBasePath());

			gradleProjectSettings.setDistributionType(DistributionType.DEFAULT_WRAPPED);
			gradleProjectSettings.setDelegatedBuild(true);
			gradleProjectSettings.setDisableWrapperSourceDistributionNotification(false);
			gradleProjectSettings.setResolveModulePerSourceSet(true);
			gradleProjectSettings.setupNewProjectDefault();
			gradleProjectSettings.setDirectDelegatedBuild(true);
			gradleProjectSettings.setUseQualifiedModuleNames(false);
			gradleProjectSettings.setResolveExternalAnnotations(true);

			GradleSettings gradleSettings = GradleSettings.getInstance(project);

			gradleSettings.linkProject(gradleProjectSettings);

			ImportSpecBuilder importSpecBuilder = new ImportSpecBuilder(project, GradleConstants.SYSTEM_ID);

			importSpecBuilder.forceWhenUptodate(true);
			importSpecBuilder.use(ProgressExecutionMode.IN_BACKGROUND_ASYNC);

			ExternalSystemUtil.refreshProject(project.getBasePath(), importSpecBuilder.build());
		};

		ExternalProjectsManager externalProjectsManager = ExternalProjectsManager.getInstance(project);

		externalProjectsManager.runWhenInitialized(
			() -> {
				DumbService dumbService = DumbService.getInstance(project);

				dumbService.runWhenSmart(
					() -> ExternalSystemUtil.ensureToolWindowInitialized(project, GradleConstants.SYSTEM_ID));
			});

		ExternalSystemUtil.invokeLater(project, ModalityState.NON_MODAL, runnable);
	}

}