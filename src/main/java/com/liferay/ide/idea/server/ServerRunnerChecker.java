/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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