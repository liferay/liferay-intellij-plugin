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

package com.liferay.ide.idea.ui.modules.spring;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.util.BladeCLI;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import icons.LiferayIcons;

import java.io.File;

import javax.swing.Icon;

import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Terry Jia
 */
public class LiferayModuleSpringMvcBuilder extends ModuleBuilder implements LiferayWorkspaceSupport {

	@Override
	public String getBuilderId() {
		Class<?> clazz = getClass();

		return clazz.getName();
	}

	@Override
	public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
		return new LiferayModuleSpringMvcWizardStep(context, this);
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

		virtualFile.refresh(true, true);

		ExternalSystemUtil.refreshProject(
			project, GradleConstants.SYSTEM_ID, project.getBasePath(), false,
			ProgressExecutionMode.IN_BACKGROUND_ASYNC);
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

	private void _createProject(VirtualFile projectRoot, Project project) {
		VirtualFile virtualFile = projectRoot.getParent();
		StringBuilder sb = new StringBuilder();

		sb.append("create -d \"");
		sb.append(virtualFile.getPath());
		sb.append("\"");
		sb.append(" ");
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
		sb.append(getLiferayVersion(project));

		BladeCLI.execute(sb.toString());
	}

	private String _framework;
	private String _frameworkDependencies;
	private String _viewType;

}