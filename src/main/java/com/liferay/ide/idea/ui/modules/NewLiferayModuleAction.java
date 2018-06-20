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

import com.intellij.ide.util.newProjectWizard.AbstractProjectWizard;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.DefaultModulesProvider;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

import icons.LiferayIcons;

import java.util.List;

import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class NewLiferayModuleAction extends AnAction implements DumbAware {

	public NewLiferayModuleAction() {
		super(LiferayIcons.LIFERAY_ICON);
	}

	@Override
	public void actionPerformed(AnActionEvent event) {
		Project project = getEventProject(event);

		if (!LiferayWorkspaceUtil.isValidWorkspaceLocation(project)) {
			Messages.showErrorDialog("Unable to detect current project as a Liferay workspace", "No Liferay workspace");

			return;
		}

		String defaultPath = null;

		VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);

		if ((virtualFile != null) && virtualFile.isDirectory()) {
			defaultPath = virtualFile.getPath();
		}

		NewLiferayModuleWizard wizard = new NewLiferayModuleWizard(
			project, new DefaultModulesProvider(project), defaultPath);

		if (wizard.showAndGet()) {
			createModuleFromWizard(project, wizard);
		}
	}

	@Nullable
	public Module createModuleFromWizard(Project project, AbstractProjectWizard wizard) {
		ProjectBuilder builder = wizard.getProjectBuilder();

		if (builder instanceof ModuleBuilder) {
			ModuleBuilder moduleBuilder = (ModuleBuilder)builder;

			if (moduleBuilder.getName() == null) {
				moduleBuilder.setName(wizard.getProjectName());
			}

			if (moduleBuilder.getModuleFilePath() == null) {
				moduleBuilder.setModuleFilePath(wizard.getModuleFilePath());
			}
		}

		if (!builder.validate(project, project)) {
			return null;
		}

		Module module = null;

		ModuleManager moduleManager = ModuleManager.getInstance(project);

		ModifiableModuleModel model = moduleManager.getModifiableModel();

		if (builder instanceof ModuleBuilder) {
			module = ((ModuleBuilder)builder).commitModule(project, model);
		}
		else {
			List<Module> modules = builder.commit(project, model, new DefaultModulesProvider(project));

			if (builder.isOpenProjectSettingsAfter()) {
				ModulesConfigurator.showDialog(project, null, null);
			}

			module = modules == null || modules.isEmpty() ? null : modules.get(0);
		}

		project.save();

		return module;
	}

	@Override
	public void update(AnActionEvent event) {
		super.update(event);

		Presentation eventPresentation = event.getPresentation();

		eventPresentation.setEnabled(LiferayWorkspaceUtil.isValidWorkspaceLocation(getEventProject(event)));
	}

}