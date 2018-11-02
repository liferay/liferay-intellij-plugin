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

package com.liferay.ide.idea.core;

import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import com.liferay.ide.idea.util.IntellijUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceUtil;
import com.liferay.ide.idea.util.WorkspaceConstants;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenImportListener;

/**
 * @author Simon Jiang
 */
public class LiferayPostStartupActivity implements StartupActivity, DumbAware {

	@Override
	public void runActivity(@NotNull Project project) {
		VirtualFile projectDir = project.getBaseDir();

		projectDir.refresh(false, true);

		MessageBus projectMessageBus = project.getMessageBus();

		MessageBusConnection messageBusConnection = projectMessageBus.connect(project);

		StartupManager startupManager = StartupManager.getInstance(project);

		startupManager.runWhenProjectIsInitialized(
			() -> messageBusConnection.subscribe(
				ProjectDataImportListener.TOPIC,
				projectPath -> {
					if (projectPath.equals(project.getBasePath())) {
						String homeDir = LiferayWorkspaceUtil.getHomeDir(project.getBasePath());

						IntellijUtil.configExcludeFolder(project, homeDir);
					}
				}));

		messageBusConnection.subscribe(
			MavenImportListener.TOPIC,
			(projects, list) -> {
				Stream<Module> modulesStream = list.stream();

				modulesStream.map(
					module -> module.getProject()
				).filter(
					moduleProject -> moduleProject.equals(project)
				).distinct(
				).forEach(
					moduleProject -> {
						String homeDir = LiferayWorkspaceUtil.getMavenProperty(
							moduleProject, WorkspaceConstants.MAVEN_HOME_DIR_PROPERTY,
							WorkspaceConstants.DEFAULT_HOME_DIR);

						IntellijUtil.configExcludeFolder(moduleProject, homeDir);
					}
				);
			});
	}

}