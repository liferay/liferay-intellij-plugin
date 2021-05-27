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

import com.intellij.openapi.externalSystem.model.ExternalSystemException;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import com.intellij.openapi.externalSystem.util.OutputWrapper;
import com.intellij.util.execution.ParametersListUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.jetbrains.annotations.Nullable;
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

		listener.onCancel(id);

		final CancellationTokenSource cancellationTokenSource = _cancellationMap.get(id);

		if (cancellationTokenSource != null) {
			cancellationTokenSource.cancel();

			return true;
		}

		return false;
	}

	@Override
	public boolean executeTasks(
			@NotNull ExternalSystemTaskId id, @NotNull List<String> taskNames, @NotNull String projectPath,
			@Nullable GradleExecutionSettings settings, @Nullable String jvmParametersSetup,
			@NotNull ExternalSystemTaskNotificationListener listener)
		throws ExternalSystemException {

		final List<String> tasks = taskNames.stream(
		).flatMap(
			s -> ParametersListUtil.parse(
				s, false, true
			).stream()
		).collect(
			Collectors.toList()
		);

		if (tasks.contains("stopDockerContainer")) {
			BuildLauncher buildLauncher = _gerBuilderLauncher(id, tasks, projectPath, settings, listener);

			_runStopDockerContainerTask(buildLauncher);

			return true;
		}

		if (tasks.contains("startDockerContainer") && tasks.contains("logsDockerContainer")) {
			BuildLauncher buildLauncher = _gerBuilderLauncher(id, tasks, projectPath, settings, listener);

			if (_stateSet.isEmpty()) {
				_runStartDockerContainerTask(id, buildLauncher);
			}

			while (true) {
				if (_isStopped.get()) {
					_runStartDockerContainerTask(id, buildLauncher);

					break;
				}
			}

			return true;
		}

		return GradleTaskManagerExtension.super.executeTasks(
			id, taskNames, projectPath, settings, jvmParametersSetup, listener);
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

		_stateSet.add("start");

		try {
			buildLauncher.run();
		}
		catch (Exception exception) {
			throw new ExternalSystemException(exception);
		}
		finally {
			_cancellationMap.remove(id);

			_isStopped.set(false);
		}
	}

	private void _runStopDockerContainerTask(BuildLauncher buildLauncher) {
		try {
			buildLauncher.run(
				new ResultHandler<Void>() {

					@Override
					public void onComplete(Void unused) {
						_isStopped.set(true);
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
	private AtomicBoolean _isStopped = new AtomicBoolean(false);
	private Set<String> _stateSet = new HashSet<>();

}