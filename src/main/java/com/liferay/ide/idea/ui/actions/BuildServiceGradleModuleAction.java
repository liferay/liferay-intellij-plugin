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