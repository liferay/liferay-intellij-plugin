/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.server;

import com.intellij.build.BuildView;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunnableState;

import com.liferay.ide.idea.util.IntellijUtil;

import org.jetbrains.annotations.NotNull;

/**
 * @author Simon Jiang
 */
public class LiferayDockerServerRunner extends GenericProgramRunner implements ServerRunnerChecker {

	@Override
	public boolean canRun(@NotNull String executorId, @NotNull RunProfile runProfile) {
		if (DefaultRunExecutor.EXECUTOR_ID.equals(executorId) &&
			(runProfile instanceof LiferayDockerServerConfiguration)) {

			return true;
		}

		return false;
	}

	@NotNull
	@Override
	public String getRunnerId() {
		return "LiferayDockerServerRunner";
	}

	@Override
	public boolean isLiferayServerRunConfiguration(RunConfiguration runConfiguration) {
		return runConfiguration instanceof LiferayDockerServerConfiguration;
	}

	@Override
	protected RunContentDescriptor doExecute(
			@NotNull RunProfileState runProfileState, @NotNull ExecutionEnvironment environment)
		throws ExecutionException {

		if (runProfileState instanceof ExternalSystemRunnableState) {
			ExecutionResult executeResult = runProfileState.execute(environment.getExecutor(), this);

			RunContentDescriptor runContentDescriptor = new RunContentBuilder(
				executeResult, environment
			).showRunContent(
				environment.getContentToReuse()
			);

			if (runContentDescriptor != null) {
				ProcessHandler processHandler = runContentDescriptor.getProcessHandler();

				if (processHandler == null) {
					return runContentDescriptor;
				}

				IntellijUtil.registerDockerSeverStopHandler(processHandler, runProfileState, environment);
			}

			ExecutionConsole executionConsole = runContentDescriptor.getExecutionConsole();

			if (executionConsole instanceof BuildView) {
				return runContentDescriptor;
			}

			RunContentDescriptor descriptor = new RunContentDescriptor(
				runContentDescriptor.getExecutionConsole(), runContentDescriptor.getProcessHandler(),
				runContentDescriptor.getComponent(), runContentDescriptor.getDisplayName(),
				runContentDescriptor.getIcon(), null, runContentDescriptor.getRestartActions());

			descriptor.setRunnerLayoutUi(runContentDescriptor.getRunnerLayoutUi());

			return descriptor;
		}

		return null;
	}

}