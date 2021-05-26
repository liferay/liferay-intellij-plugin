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

package com.liferay.ide.idea.ui.modules.ext;

import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.projectWizard.ProjectTypeStep;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.ui.modules.LiferayProjectSettingsStep;
import com.liferay.ide.idea.util.BladeCLI;
import com.liferay.ide.idea.util.IntellijUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Terry Jia
 * @author Simon Jiang
 * @author Charles Wu
 */
public class LiferayModuleExtBuilder extends ModuleBuilder implements LiferayWorkspaceSupport {

	public LiferayModuleExtBuilder() {
		addListener(
			new ModuleBuilderListener() {

				@Override
				public void moduleCreated(@NotNull Module module) {
					Project project = module.getProject();

					ExternalSystemUtil.refreshProject(
						project, GradleConstants.SYSTEM_ID, project.getBasePath(), false,
						ProgressExecutionMode.IN_BACKGROUND_ASYNC);

					removeListener(this);
				}

			});
	}

	@Override
	public ModuleWizardStep[] createFinishingSteps(
		@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {

		return new ModuleWizardStep[] {new LiferayModuleExtWizardStep(wizardContext, this)};
	}

	@Override
	public ModuleWizardStep[] createWizardSteps(
		@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {

		return new ModuleWizardStep[] {new LiferayProjectSettingsStep(wizardContext)};
	}

	@Override
	public String getBuilderId() {
		Class<?> clazz = getClass();

		return clazz.getName();
	}

	@Override
	public String getDescription() {
		return _LIFERAY_EXT_MODULES;
	}

	@NotNull
	public List<Class<? extends ModuleWizardStep>> getIgnoredSteps() {
		List<Class<? extends ModuleWizardStep>> ingoreStepList = new ArrayList<>(super.getIgnoredSteps());

		ingoreStepList.add(ProjectTypeStep.class);
		ingoreStepList.add(ProjectSettingsStep.class);

		return ingoreStepList;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public ModuleType getModuleType() {
		return StdModuleTypes.JAVA;
	}

	@Override
	public Icon getNodeIcon() {
		return LiferayIcons.LIFERAY_ICON;
	}

	@Override
	public String getPresentableName() {
		return _LIFERAY_EXT_MODULES;
	}

	public void setOriginalModuleName(String originalModuleName) {
		_originalModuleName = originalModuleName;
	}

	public void setOriginalModuleVersion(String originalModuleVersion) {
		_originalModuleVersion = originalModuleVersion;
	}

	@Override
	public void setupRootModel(ModifiableRootModel modifiableRootModel) {
		VirtualFile virtualFile = _createAndGetContentEntry();
		Project project = modifiableRootModel.getProject();

		_createProject(virtualFile, project);

		modifiableRootModel.addContentEntry(virtualFile);

		if (myJdk != null) {
			modifiableRootModel.setSdk(myJdk);
		}
		else {
			modifiableRootModel.inheritSdk();
		}

		_refreshProject(project);
	}

	@Override
	public boolean validateModuleName(@NotNull String moduleName) {
		return IntellijUtil.validateExistingModuleName(moduleName);
	}

	private VirtualFile _createAndGetContentEntry() {
		String path = FileUtilRt.toSystemIndependentName(getContentEntryPath());

		File file = new File(path);

		file.mkdirs();

		LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

		return localFileSystem.refreshAndFindFileByPath(path);
	}

	private void _createProject(VirtualFile projectRoot, Project project) {
		VirtualFile virtualFile = projectRoot.getParent();
		StringBuilder sb = new StringBuilder();

		sb.append("create -d \"");
		sb.append(virtualFile.getPath());
		sb.append("\" ");
		sb.append("--base \"");
		sb.append(project.getBasePath());
		sb.append("\" -t ");
		sb.append("modules-ext ");
		sb.append("-m ");
		sb.append(_originalModuleName);

		if (Objects.nonNull(project) && !LiferayWorkspaceSupport.isFlexibleLiferayWorkspace(project)) {
			sb.append(" -M ");
			sb.append(_originalModuleVersion);
		}

		sb.append(" ");
		sb.append("\"");
		sb.append(projectRoot.getName());
		sb.append("\"");

		BladeCLI.execute(sb.toString());
	}

	private void _refreshProject(Project project) {
		VirtualFile projectDir = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (projectDir == null) {
			return;
		}

		projectDir.refresh(false, true);
	}

	private static final String _LIFERAY_EXT_MODULES = "Liferay Modules Ext";

	private String _originalModuleName;
	private String _originalModuleVersion;

}