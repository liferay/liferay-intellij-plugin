/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.core;

import com.intellij.openapi.externalSystem.model.ExternalSystemException;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import com.intellij.openapi.externalSystem.util.OutputWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.util.execution.ParametersListUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.service.execution.GradleProgressListener;
import org.jetbrains.plugins.gradle.service.task.GradleTaskManagerExtension;
import org.jetbrains.plugins.gradle.settings.GradleExecutionSettings;

/**
 * @author Simon Jiang
 */
public class LiferayGradleTaskManager implements GradleTaskManagerExtension {

	@Override
	public boolean cancelTask(
			@NotNull ExternalSystemTaskId id, @NotNull ExternalSystemTaskNotificationListener listener)
		throws ExternalSystemException {

		Project project = id.findProject();

		if (project == null) {
			return false;
		}

		String projectPath = project.getProjectFilePath();

		if (projectPath == null) {
			return false;
		}

		listener.onCancel(projectPath, id);

		final CancellationTokenSource cancellationTokenSource = _cancellationMap.get(id);

		if (cancellationTokenSource != null) {
			cancellationTokenSource.cancel();
			_cancellationMap.remove(id);

			return true;
		}

		return false;
	}

	@Override
	public boolean executeTasks(
			@NotNull String projectPath, @NotNull ExternalSystemTaskId id, @NotNull GradleExecutionSettings settings,
			@NotNull ExternalSystemTaskNotificationListener listener)
		throws ExternalSystemException {

		final List<String> tasks = settings.getTasks(
		).stream(
		).flatMap(
			s -> ParametersListUtil.parse(
				s, false, true
			).stream()
		).collect(
			Collectors.toList()
		);

		if (tasks.contains("stopDockerContainer")) {
			BuildLauncher buildLauncher = _gerBuilderLauncher(id, tasks, projectPath, settings, listener);

			_runStopDockerContainerTask(id, buildLauncher);

			return true;
		}

		if (tasks.contains("startDockerContainer") && tasks.contains("logsDockerContainer")) {
			BuildLauncher buildLauncher = _gerBuilderLauncher(id, tasks, projectPath, settings, listener);

			while (true) {
				if (_isStopped.get()) {
					_runStartDockerContainerTask(id, buildLauncher);

					break;
				}
			}

			return true;
		}

		return GradleTaskManagerExtension.super.executeTasks(projectPath, id, settings, listener);
	}

	private BuildLauncher _gerBuilderLauncher(
		ExternalSystemTaskId id, List<String> tasks, String projectPath, GradleExecutionSettings settings,
		ExternalSystemTaskNotificationListener listener) {

		GradleConnector gradleConnector = GradleConnector.newConnector();

		Path projectVirtualFilePath = Paths.get(projectPath);

		gradleConnector.forProjectDirectory(projectVirtualFilePath.toFile());

		ProjectConnection connection = gradleConnector.connect();

		BuildLauncher buildLauncher = connection.newBuild();

		buildLauncher.addArguments(settings.getArguments());

		buildLauncher.forTasks(tasks.toArray(new String[0]));

		GradleProgressListener gradleProgressListener = new GradleProgressListener(listener, id);

		buildLauncher.addProgressListener((ProgressListener)gradleProgressListener);

		buildLauncher.addProgressListener((org.gradle.tooling.events.ProgressListener)gradleProgressListener);

		buildLauncher.setStandardOutput(new OutputWrapper(listener, id, true));

		buildLauncher.setStandardError(new OutputWrapper(listener, id, false));

		return buildLauncher;
	}

	private void _runStartDockerContainerTask(ExternalSystemTaskId id, BuildLauncher buildLauncher) {
		CancellationTokenSource cancellationTokenSource = GradleConnector.newCancellationTokenSource();

		_cancellationMap.put(id, cancellationTokenSource);

		buildLauncher.withCancellationToken(cancellationTokenSource.token());

		try {
			buildLauncher.run();
		}
		catch (Exception exception) {
			throw new ExternalSystemException(exception);
		}
		finally {
			_isStopped.set(false);
		}
	}

	private void _runStopDockerContainerTask(ExternalSystemTaskId id, BuildLauncher buildLauncher) {
		try {
			buildLauncher.run(
				new ResultHandler<Void>() {

					@Override
					public void onComplete(Void unused) {
						_isStopped.set(true);
						_cancellationMap.remove(id);
					}

					@Override
					public void onFailure(GradleConnectionException gradleConnectionException) {
					}

				});
		}
		catch (Exception exception) {
			throw new ExternalSystemException(exception);
		}
	}

	private final Map<ExternalSystemTaskId, CancellationTokenSource> _cancellationMap = new ConcurrentHashMap<>();
	private AtomicBoolean _isStopped = new AtomicBoolean(true);

}