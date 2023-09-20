/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.util.List;
import java.util.Objects;

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
	protected void doExecute(
		AnActionEvent anActionEvent, RunnerAndConfigurationSettings runnerAndConfigurationSettings) {

		Project project = MavenActionUtil.getProject(anActionEvent.getDataContext());

		if (Objects.isNull(project)) {
			return;
		}

		ProgramRunner<?> programRunner = DefaultJavaProgramRunner.getInstance();

		try {
			ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(
				DefaultRunExecutor.getRunExecutorInstance(), programRunner, runnerAndConfigurationSettings, project);

			programRunner.execute(executionEnvironment);
		}
		catch (ExecutionException executionException) {
			MavenUtil.showError(project, "Failed to execute Maven goal", executionException);
		}
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		project = anActionEvent.getProject();

		if (!LiferayWorkspaceSupport.isValidMavenWorkspaceProject(project)) {
			return false;
		}

		VirtualFile virtualFile = getVirtualFile(anActionEvent);

		if ((project == null) || (virtualFile == null)) {
			return false;
		}

		Module module = ModuleUtil.findModuleForFile(virtualFile, project);

		if (Objects.isNull(module)) {
			return false;
		}

		return true;
	}

	@Nullable
	@Override
	protected RunnerAndConfigurationSettings processRunnerConfiguration(AnActionEvent anActionEvent) {
		DataContext dataContext = anActionEvent.getDataContext();

		Project project = MavenActionUtil.getProject(dataContext);

		MavenProject mavenProject = MavenActionUtil.getMavenProject(dataContext);

		if ((project == null) || (mavenProject == null)) {
			return null;
		}

		VirtualFile mavenProjectDirectoryFile = mavenProject.getDirectoryFile();

		VirtualFile pomFile = mavenProjectDirectoryFile.findChild("pom.xml");

		if (Objects.isNull(pomFile)) {
			return null;
		}

		MavenProjectsManager mavenProjectsManager = MavenActionUtil.getProjectsManager(dataContext);

		if (Objects.isNull(mavenProjectsManager)) {
			return null;
		}

		String projectDir = mavenProject.getDirectory();

		MavenExplicitProfiles explicitProfiles = mavenProjectsManager.getExplicitProfiles();

		MavenRunnerParameters params = new MavenRunnerParameters(
			true, projectDir, pomFile.getName(), goals, explicitProfiles.getEnabledProfiles(),
			explicitProfiles.getDisabledProfiles());

		return MavenRunConfigurationType.createRunnerAndConfigurationSettings(null, null, params, project);
	}

	protected List<String> goals;
	protected Project project;

}