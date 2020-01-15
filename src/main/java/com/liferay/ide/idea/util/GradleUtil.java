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

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;

import java.util.List;
import java.util.Objects;

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

			if ((gradleProjectSettings != null) && !gradleProjectSettings.isUseAutoImport()) {
				ExternalSystemUtil.refreshProjects(new ImportSpecBuilder(project, GradleConstants.SYSTEM_ID));
			}
		}
	}

	public static boolean isWatchableProject(Module module) {
		Project project = module.getProject();

		RunContentManager runContentManager = RunContentManager.getInstance(project);

		List<RunContentDescriptor> allDescriptors = runContentManager.getAllDescriptors();

		for (RunContentDescriptor descriptor : allDescriptors) {
			ProcessHandler processHandler = descriptor.getProcessHandler();

			boolean processTerminated = processHandler.isProcessTerminated();

			if (Objects.equals(project.getName() + " [watch]", descriptor.getDisplayName()) && !processTerminated) {
				return false;
			}
		}

		GradleExtensionsSettings.Settings settings = GradleExtensionsSettings.getInstance(project);

		GradleExtensionsSettings.GradleExtensionsData gradleExtensionsData = settings.getExtensionsFor(module);

		if (gradleExtensionsData == null) {
			return false;
		}

		for (GradleExtensionsSettings.GradleTask gradleTask : gradleExtensionsData.tasks) {
			if (Objects.equals("watch", gradleTask.name) &&
				Objects.equals("com.liferay.gradle.plugins.tasks.WatchTask", gradleTask.typeFqn)) {

				return true;
			}
		}

		return false;
	}

}