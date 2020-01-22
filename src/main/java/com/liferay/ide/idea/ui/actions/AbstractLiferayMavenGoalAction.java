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

package com.liferay.ide.idea.ui.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.model.MavenExplicitProfiles;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenUtil;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

/**
 * @author Joye Luo
 * @author Simon Jiang
 */
public abstract class AbstractLiferayMavenGoalAction extends AbstractLiferayAction {

	public AbstractLiferayMavenGoalAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	@Nullable
	@Override
	protected void doExecute(AnActionEvent anActionEvent, RunnerAndConfigurationSettings configuration) {
		Project project = MavenActionUtil.getProject(anActionEvent.getDataContext());

		ProgramRunner<?> programRunner = DefaultJavaProgramRunner.getInstance();

		Executor executor = DefaultRunExecutor.getRunExecutorInstance();

		try {
			programRunner.execute(new ExecutionEnvironment(executor, programRunner, configuration, project), null);
		}
		catch (ExecutionException ee) {
			MavenUtil.showError(project, "Failed to execute Maven goal", ee);
		}
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		if (LiferayWorkspaceSupport.isValidMavenWorkspaceLocation(anActionEvent.getProject())) {
			return true;
		}

		return false;
	}

	@Nullable
	@Override
	protected RunnerAndConfigurationSettings processRunnerConifugration(AnActionEvent anActionEvent) {
		DataContext dataContext = anActionEvent.getDataContext();

		Project project = MavenActionUtil.getProject(dataContext);

		MavenProject mavenProject = MavenActionUtil.getMavenProject(dataContext);

		if ((project == null) || (mavenProject == null)) {
			return null;
		}

		String projectDir = mavenProject.getDirectory();

		VirtualFile mavenProjectDirectoryFile = mavenProject.getDirectoryFile();

		VirtualFile pomFile = mavenProjectDirectoryFile.findChild("pom.xml");

		MavenProjectsManager mavenProjectsManager = MavenActionUtil.getProjectsManager(dataContext);

		MavenExplicitProfiles explicitProfiles = mavenProjectsManager.getExplicitProfiles();

		MavenRunnerParameters params = new MavenRunnerParameters(
			true, projectDir, pomFile.getName(), goals, explicitProfiles.getEnabledProfiles(),
			explicitProfiles.getDisabledProfiles());

		return MavenRunConfigurationType.createRunnerAndConfigurationSettings(null, null, params, project);
	}

	protected List<String> goals;

}