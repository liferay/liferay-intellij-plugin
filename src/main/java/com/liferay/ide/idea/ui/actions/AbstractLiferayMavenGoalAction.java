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

import java.util.Collection;
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

	@Override
	public boolean isEnabledAndVisible(AnActionEvent event) {
		Project project = event.getProject();

		VirtualFile baseDir = project.getBaseDir();

		VirtualFile pomFile = baseDir.findChild("pom.xml");

		if (baseDir.equals(getVirtualFile(event)) && (pomFile != null)) {
			return true;
		}

		return false;
	}

	@Override
	protected RunnerAndConfigurationSettings doExecute(AnActionEvent event) {
		projectDir = getWorkingDirectory(event);

		DataContext dataContext = event.getDataContext();

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

		Collection<String> enabledProfiles = explicitProfiles.getEnabledProfiles();
		Collection<String> disabledProfiles = explicitProfiles.getDisabledProfiles();

		MavenRunnerParameters params = new MavenRunnerParameters(
			true, projectDir, pomFile.getName(), goals, enabledProfiles, disabledProfiles);

		if (params == null) {
			return null;
		}

		RunnerAndConfigurationSettings configuration = MavenRunConfigurationType.createRunnerAndConfigurationSettings(
			null, null, params, project);

		ProgramRunner runner = DefaultJavaProgramRunner.getInstance();
		Executor executor = DefaultRunExecutor.getRunExecutorInstance();

		try {
			runner.execute(new ExecutionEnvironment(executor, runner, configuration, project), null);
		}
		catch (ExecutionException ee) {
			MavenUtil.showError(project, "Failed to execute Maven goal", ee);
		}

		return configuration;
	}

	protected List<String> goals;

}