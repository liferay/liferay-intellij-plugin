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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.model.Container;

import com.google.common.collect.Lists;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;

import com.liferay.blade.gradle.tooling.ProjectInfo;
import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.server.LiferayDockerServerConfigurationProducer;
import com.liferay.ide.idea.server.LiferayDockerServerConfigurationType;
import com.liferay.ide.idea.util.LiferayDockerClient;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ListUtil;


import java.util.List;
import java.util.Objects;

/**
 * @author Simon Jiang
 */
import static com.liferay.ide.idea.util.GradleUtil.getModel;

/**
 * @author Simon Jiang
 */
public class InitDockerBundleAction extends AbstractLiferayGradleTaskAction implements LiferayWorkspaceSupport {

	public InitDockerBundleAction() {
		super("InitDockerBundle", "Run init docker Bundle task", LiferayIcons.LIFERAY_ICON, "createDockerContainer");
	}

	@Override
	protected void afterTask(Project project) {
		List<RunConfigurationProducer<?>> producers = LiferayDockerServerConfigurationProducer.getProducers(project);

		for (RunConfigurationProducer producer : producers) {
			ConfigurationType configurationType = producer.getConfigurationType();

			if (Objects.equals(LiferayDockerServerConfigurationType.id, configurationType.getId())) {
				ConfigurationFactory configurationFactory = producer.getConfigurationFactory();

				RunManager runManager = RunManager.getInstance(project);

				RunnerAndConfigurationSettings configuration = runManager.findConfigurationByTypeAndName(
					configurationType, project.getName() + "-docker-server");

				if (configuration == null) {
					runManager.addConfiguration(
						runManager.createConfiguration(project.getName() + "-docker-server", configurationFactory));
				}
			}
		}
	}

	@Override
	protected void beforeTask(Project project) {
		try (DockerClient dockerClient = LiferayDockerClient.getDockerClient()) {
			ProjectInfo projectInfo = getModel(ProjectInfo.class, ProjectUtil.guessProjectDir(project));

			ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();

			listContainersCmd.withNameFilter(Lists.newArrayList(projectInfo.getDockerContainerId()));
			listContainersCmd.withLimit(1);

			List<Container> containers = listContainersCmd.exec();

			if (ListUtil.isNotEmpty(containers)) {
				Container container = containers.get(0);

				RemoveContainerCmd removeContainerCmd = dockerClient.removeContainerCmd(container.getId());

				removeContainerCmd.exec();
			}
		}
		catch (Exception e) {
			_logger.error(e);
		}
	}

	private static final Logger _logger = Logger.getInstance(InitDockerBundleAction.class);

}