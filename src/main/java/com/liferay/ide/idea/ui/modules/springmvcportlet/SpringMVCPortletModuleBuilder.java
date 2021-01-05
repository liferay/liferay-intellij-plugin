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

package com.liferay.ide.idea.ui.modules.springmvcportlet;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.core.LiferayProjectTypeService;
import com.liferay.ide.idea.ui.modules.LiferayProjectType;
import com.liferay.ide.idea.util.BladeCLI;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.io.File;

import java.util.Objects;

import javax.swing.Icon;

import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Terry Jia
 */
public class SpringMVCPortletModuleBuilder extends ModuleBuilder implements LiferayWorkspaceSupport {

	@Override
	public String getBuilderId() {
		Class<?> clazz = getClass();

		return clazz.getName();
	}

	@Override
	public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
		return new SpringMvcPortletModuleWizardStep(context, this);
	}

	@Override
	public String getDescription() {
		return "Liferay Spring MVC Portlet";
	}

	@Override
	@SuppressWarnings("rawtypes")
	public ModuleType getModuleType() {
		return StdModuleTypes.JAVA;
	}

	@Override
	public Icon getNodeIcon() {
		return LiferayIcons.SPRING_ICON;
	}

	@Override
	public String getPresentableName() {
		return "Liferay Spring MVC Portlet";
	}

	public void setFramework(String framework) {
		_framework = framework;
	}

	public void setFrameworkDependencies(String frameworkDependencies) {
		_frameworkDependencies = frameworkDependencies;
	}

	public void setLiferayVersion(String liferayVersion) {
		_liferayVersion = liferayVersion;
	}

	@Override
	public void setupRootModel(ModifiableRootModel modifiableRootModel) {
		VirtualFile virtualFile = _createAndGetContentEntry();

		Project project = modifiableRootModel.getProject();

		ProjectType projectType = LiferayProjectTypeService.getProjectType(project);

		String typeId = projectType.getId();

		_createProject(virtualFile, project, typeId);

		modifiableRootModel.addContentEntry(virtualFile);

		if (myJdk != null) {
			modifiableRootModel.setSdk(myJdk);
		}
		else {
			modifiableRootModel.inheritSdk();
		}

		_refreshProject(project, typeId);
	}

	public void setViewType(String viewType) {
		_viewType = viewType;
	}

	private VirtualFile _createAndGetContentEntry() {
		String path = FileUtilRt.toSystemIndependentName(getContentEntryPath());

		File file = new File(path);

		file.mkdirs();

		LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

		return localFileSystem.refreshAndFindFileByPath(path);
	}

	private void _createProject(VirtualFile projectRoot, Project project, String typeId) {
		VirtualFile virtualFile = projectRoot.getParent();
		StringBuilder sb = new StringBuilder();

		sb.append("create -d \"");
		sb.append(virtualFile.getPath());
		sb.append("\"");
		sb.append(" ");

		if (Objects.equals(typeId, LiferayProjectType.LIFERAY_MAVEN_WORKSPACE)) {
			sb.append("-b ");
			sb.append("maven ");
		}

		sb.append("--base \"");
		sb.append(project.getBasePath());
		sb.append("\" -t ");
		sb.append("spring-mvc-portlet ");
		sb.append("\"");
		sb.append(projectRoot.getName());
		sb.append("\"");
		sb.append(" ");
		sb.append("--framework ");
		sb.append(_framework);
		sb.append(" ");
		sb.append("--framework-dependencies ");
		sb.append(_frameworkDependencies);
		sb.append(" ");
		sb.append("--view-type ");
		sb.append(_viewType);
		sb.append(" ");
		sb.append("-v ");
		sb.append(_liferayVersion);

		BladeCLI.execute(sb.toString());
	}

	private void _refreshProject(Project project, String typeId) {
		VirtualFile projectDir = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (projectDir == null) {
			return;
		}

		projectDir.refresh(false, true);

		if (Objects.equals(typeId, LiferayProjectType.LIFERAY_MAVEN_WORKSPACE)) {
			return;
		}

		ExternalSystemUtil.refreshProject(
			project, GradleConstants.SYSTEM_ID, projectDir.getPath(), false, ProgressExecutionMode.IN_BACKGROUND_ASYNC);
	}

	private String _framework;
	private String _frameworkDependencies;
	private String _liferayVersion;
	private String _viewType;

}