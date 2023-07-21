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

package com.liferay.ide.idea.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * @author Simon Jiang
 */
public interface ServerRunnerChecker {

	public boolean isLiferayServerRunConfiguration(RunConfiguration runConfiguration);

	public default void verifyRunningServer(@NotNull ExecutionEnvironment environment) throws ExecutionException {
		RunManager runManager = RunManager.getInstance(environment.getProject());

		List<RunConfiguration> allConfigurationsList = runManager.getAllConfigurationsList();

		List<String> runConfigurationNames = new ArrayList<>();

		for (RunConfiguration configuration : allConfigurationsList) {
			if (isLiferayServerRunConfiguration(configuration)) {
				runConfigurationNames.add(configuration.getName());
			}
		}

		ExecutionManager executionManager = ExecutionManager.getInstance(environment.getProject());

		RunContentManager runContentManager = RunContentManager.getInstance(environment.getProject());

		for (RunContentDescriptor runContentDescriptor : runContentManager.getAllDescriptors()) {
			if (runConfigurationNames.contains(runContentDescriptor.getDisplayName())) {
				ProcessHandler processHandler = runContentDescriptor.getProcessHandler();

				boolean hasRunningServer = Arrays.stream(
					executionManager.getRunningProcesses()
				).filter(
					process -> {
						if (process.equals(processHandler) && !processHandler.isProcessTerminated() &&
							!processHandler.isProcessTerminating()) {

							return true;
						}

						return false;
					}
				).findAny(
				).isPresent();

				if (hasRunningServer) {
					throw new ExecutionException(
						MessageFormat.format(
							"Found another server {0} is running", runContentDescriptor.getDisplayName()));
				}
			}
		}
	}

}