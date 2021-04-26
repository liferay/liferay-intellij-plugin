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

import com.intellij.build.BuildView;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
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

import org.jetbrains.annotations.NotNull;

/**
 * @author Simon Jiang
 */
public class LiferayDockerServerRunner extends GenericProgramRunner implements ILiferayDockerServerRunnerCallback {

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

				registerDockerSeverStopHandler(processHandler, runProfileState, environment);
			}

			ExecutionConsole executionConsole = runContentDescriptor.getExecutionConsole();

			if (executionConsole instanceof BuildView) {
				return runContentDescriptor;
			}

			RunContentDescriptor descriptor = new RunContentDescriptor(
				runContentDescriptor.getExecutionConsole(), runContentDescriptor.getProcessHandler(),
				runContentDescriptor.getComponent(), runContentDescriptor.getDisplayName(),
				runContentDescriptor.getIcon(), null, runContentDescriptor.getRestartActions()) {

				@Override
				public boolean isHiddenContent() {
					return true;
				}

			};

			descriptor.setRunnerLayoutUi(runContentDescriptor.getRunnerLayoutUi());

			return descriptor;
		}

		return null;
	}

}