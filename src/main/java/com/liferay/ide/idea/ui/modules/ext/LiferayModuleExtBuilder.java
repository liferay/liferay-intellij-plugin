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
import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

import icons.LiferayIcons;

import java.io.File;

import javax.swing.Icon;

import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Terry Jia
 * @author Simon Jiang
 * @author Charles Wu
 */
public class LiferayModuleExtBuilder extends ModuleBuilder {

	@Override
	public String getBuilderId() {
		Class<?> clazz = getClass();

		return clazz.getName();
	}

	@Override
	public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
		return new LiferayModuleExtWizardStep(context, this);
	}

	@Override
	public String getDescription() {
		return _LIFERAY_EXT_MODULES;
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

	public void setOverrideFilesPanel(OverrideFilesComponent overrideFilesPanel) {
		_overrideFilesPanel = overrideFilesPanel;
	}

	@Override
	public void setupRootModel(ModifiableRootModel rootModel) {
		VirtualFile virtualFile = _createAndGetContentEntry();
		Project project = rootModel.getProject();

		_createProject(virtualFile, project);

		rootModel.addContentEntry(virtualFile);

		if (myJdk != null) {
			rootModel.setSdk(myJdk);
		}
		else {
			rootModel.inheritSdk();
		}

		virtualFile.refresh(true, true);

		_overrideFilesPanel.doFinish(virtualFile);

		ExternalSystemUtil.refreshProject(
			project, GradleConstants.SYSTEM_ID, project.getBasePath(), false,
			ProgressExecutionMode.IN_BACKGROUND_ASYNC);
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

		if (LiferayWorkspaceUtil.getTargetPlatformVersion(project) == null) {
			sb.append(" -M ");
			sb.append(_originalModuleVersion);
		}

		sb.append(" ");
		sb.append("\"");
		sb.append(projectRoot.getName());
		sb.append("\"");

		BladeCLI.execute(sb.toString());
	}

	private static final String _LIFERAY_EXT_MODULES = "Liferay Modules Ext";

	private String _originalModuleName;
	private String _originalModuleVersion;
	private OverrideFilesComponent _overrideFilesPanel;

}