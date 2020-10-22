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

package com.liferay.ide.idea.util;

import com.google.common.collect.ListMultimap;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.ExternalProjectInfo;
import com.intellij.openapi.externalSystem.model.ProjectKeys;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;

import org.jetbrains.plugins.gradle.settings.GradleExtensionsSettings;
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings;
import org.jetbrains.plugins.gradle.settings.GradleSettings;
import org.jetbrains.plugins.gradle.util.GradleConstants;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

/**
 * @author Terry Jia
 * @author Charles Wu
 * @author Ethan Sun
 */
public class GradleUtil {

	/**
	 * @param file build.gradle file
	 */
	public static void addGradleDependencies(PsiFile file, String... dependencies) {
		Project project = file.getProject();

		WriteCommandAction.Builder builder = WriteCommandAction.writeCommandAction(project, file);

		builder.withName(
			"Add Gradle Dependency"
		).run(
			() -> {
				GroovyPsiElementFactory groovyPsiElementFactory = GroovyPsiElementFactory.getInstance(project);

				List<GrMethodCall> grMethodCalls = PsiTreeUtil.getChildrenOfTypeAsList(file, GrMethodCall.class);

				GrCall dependenciesBlock = ContainerUtil.find(
					grMethodCalls,
					call -> {
						GrExpression grExpression = call.getInvokedExpression();

						return Objects.equals("dependencies", grExpression.getText());
					});

				if (dependenciesBlock == null) {
					StringBuilder stringBuilder = new StringBuilder();

					for (String dependency : dependencies) {
						stringBuilder.append(String.format("compileOnly '%s'\n", dependency));
					}

					dependenciesBlock = (GrCall)groovyPsiElementFactory.createStatementFromText(
						"dependencies{\n" + stringBuilder + "}");

					file.add(dependenciesBlock);
				}
				else {
					GrClosableBlock grClosableBlock = ArrayUtil.getFirstElement(
						dependenciesBlock.getClosureArguments());

					if (grClosableBlock != null) {
						for (String dependency : dependencies) {
							grClosableBlock.addStatementBefore(
								groovyPsiElementFactory.createStatementFromText(
									String.format("compileOnly '%s'\n", dependency)),
								null);
						}
					}
				}
			}
		);

		GradleSettings gradleSettings = GradleSettings.getInstance(project);

		String projectRoot = project.getBasePath();

		if (projectRoot != null) {
			GradleProjectSettings gradleProjectSettings = gradleSettings.getLinkedProjectSettings(projectRoot);

			if (gradleProjectSettings != null) {
				ExternalSystemUtil.refreshProjects(new ImportSpecBuilder(project, GradleConstants.SYSTEM_ID));
			}
		}
	}

	public static List<LibraryData> getTargetPlatformArtifacts(Project project) {
		ProjectDataManager projectDataManager = ProjectDataManager.getInstance();

		Collection<ExternalProjectInfo> externalProjectInfos = projectDataManager.getExternalProjectsData(
			project, GradleConstants.SYSTEM_ID);

		for (ExternalProjectInfo externalProjectInfo : externalProjectInfos) {
			DataNode<ProjectData> projectData = externalProjectInfo.getExternalProjectStructure();

			if (projectData == null) {
				continue;
			}

			Collection<DataNode<?>> dataNodes = projectData.getChildren();

			List<LibraryData> libraryData = new ArrayList<>(dataNodes.size());

			for (DataNode<?> child : dataNodes) {
				if (!ProjectKeys.LIBRARY.equals(child.getKey())) {
					continue;
				}

				libraryData.add((LibraryData)child.getData());
			}

			libraryData.sort(
				Comparator.comparing(LibraryData::getArtifactId, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)));

			return libraryData;
		}

		return Collections.emptyList();
	}

	public static GradleProject getWorkspaceGradleProject(Project project) {
		Path pathToGradleProject = Paths.get(project.getBasePath());

		GradleConnector gradleConnector = GradleConnector.newConnector(
		).forProjectDirectory(
			pathToGradleProject.toFile()
		);

		try (ProjectConnection projectConnection = gradleConnector.connect()) {
			ModelBuilder<GradleProject> modelBuilder = projectConnection.model(GradleProject.class);

			return modelBuilder.get();
		}
	}

	public static String getWorkspacePluginVersion(Project project) {
		File settingsGradleFile = new File(project.getBasePath(), "settings.gradle");

		GradleDependencyUpdater gradleDependencyUpdater = null;

		try {
			gradleDependencyUpdater = new GradleDependencyUpdater(settingsGradleFile);
		}
		catch (IOException ioe) {
		}

		return Optional.ofNullable(
			gradleDependencyUpdater
		).flatMap(
			updater -> {
				ListMultimap<String, GradleDependency> dependencies = updater.getAllDependencies();

				List<GradleDependency> artifacts = new ArrayList<>(dependencies.values());

				return artifacts.stream(
				).filter(
					artifact -> Objects.equals("com.liferay", artifact.getGroup())
				).filter(
					artifact -> Objects.equals("com.liferay.gradle.plugins.workspace", artifact.getName())
				).filter(
					artifact -> !CoreUtil.isNullOrEmpty(artifact.getVersion())
				).map(
					GradleDependency::getVersion
				).findFirst();
			}
		).orElseGet(
			() -> "2.2.4"
		);
	}

	public static boolean isWatchableProject(Module module) {
		Project project = module.getProject();

		RunContentManager runContentManager = RunContentManager.getInstance(project);

		List<RunContentDescriptor> allDescriptors = runContentManager.getAllDescriptors();

		for (RunContentDescriptor descriptor : allDescriptors) {
			ProcessHandler processHandler = descriptor.getProcessHandler();

			if (processHandler != null) {
				boolean processTerminated = processHandler.isProcessTerminated();

				if (Objects.equals(project.getName() + " [watch]", descriptor.getDisplayName()) && !processTerminated) {
					return false;
				}
			}
		}

		GradleExtensionsSettings.Settings settings = GradleExtensionsSettings.getInstance(project);

		GradleExtensionsSettings.GradleExtensionsData gradleExtensionsData = settings.getExtensionsFor(module);

		if (gradleExtensionsData == null) {
			return false;
		}

		return gradleExtensionsData.tasksMap.entrySet(
		).stream(
		).map(
			entry -> entry.getValue()
		).filter(
			task -> Objects.equals("watch", task.name)
		).filter(
			task -> Objects.deepEquals("com.liferay.gradle.plugins.tasks.WatchTask", task.typeFqn)
		).findAny(
		).isPresent();
	}

}