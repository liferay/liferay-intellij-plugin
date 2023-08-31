/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ListUtil;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleTask;

/**
 * @author Ethan Sun
 * @author Simon Jiang
 */
public class BuildServiceGradleModuleAction extends AbstractLiferayGradleTaskAction implements LiferayWorkspaceSupport {

	public BuildServiceGradleModuleAction() {
		super("BuildService", "Run buildService task", LiferayIcons.LIFERAY_ICON, "buildService");
	}

	@Override
	protected List<String> getGradleTasks() {
		Stream<Module> moduleStream = _moduleSet.stream();

		return moduleStream.map(
			GradleUtil::getGradleProject
		).flatMap(
			gradleModule -> {
				DomainObjectSet<? extends GradleTask> gradleModuleTasks = gradleModule.getTasks();

				Stream<? extends GradleTask> buildServiceTaskStream = gradleModuleTasks.stream();

				return buildServiceTaskStream.filter(task -> Objects.equals(task.getName(), "buildService"));
			}
		).map(
			task -> task.getPath()
		).collect(
			Collectors.toList()
		);
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		if (super.isEnabledAndVisible(anActionEvent)) {
			_moduleSet = getServiceBuilderModules(anActionEvent);

			return ListUtil.isNotEmpty(_moduleSet);
		}

		return false;
	}

	private Set<Module> _moduleSet;

}