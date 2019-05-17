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

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemProcessHandler;
import com.intellij.openapi.project.Project;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class LiferayServerDebuggerRunner extends GenericDebuggerRunner {

	@Override
	public boolean canRun(@NotNull String executorId, @NotNull RunProfile runProfile) {
		if (DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && (runProfile instanceof LiferayServerConfiguration)) {
			return true;
		}

		return false;
	}

	@NotNull
	@Override
	public String getRunnerId() {
		return "LiferayServerDebugger";
	}

	@Nullable
	@Override
	protected RunContentDescriptor doExecute(
			@NotNull RunProfileState runProfileState, @NotNull ExecutionEnvironment executionEnvironment)
		throws ExecutionException {

		DebuggerSettings debuggerSettings = DebuggerSettings.getInstance();

		RunContentDescriptor runContentDescriptor;

		if (debuggerSettings.INSTRUMENTING_AGENT) {
			debuggerSettings.INSTRUMENTING_AGENT = false;

			runContentDescriptor = super.doExecute(runProfileState, executionEnvironment);

			debuggerSettings.INSTRUMENTING_AGENT = true;
		}
		else {
			runContentDescriptor = super.doExecute(runProfileState, executionEnvironment);
		}

		if (runContentDescriptor != null) {
			ProcessHandler processHandler = runContentDescriptor.getProcessHandler();

			if (processHandler == null) {
				return runContentDescriptor;
			}

			processHandler.addProcessListener(
				new ProcessAdapter() {

					@Override
					public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed) {
						Project project = executionEnvironment.getProject();

						ExecutionManager executionManager = ExecutionManager.getInstance(project);

						ProcessHandler[] runningProcesses = executionManager.getRunningProcesses();

						Stream.of(
							runningProcesses
						).filter(
							process -> !process.equals(event.getProcessHandler())
						).filter(
							process -> process instanceof ExternalSystemProcessHandler
						).forEach(
							process -> {
								ExternalSystemProcessHandler exProcessHandler = (ExternalSystemProcessHandler)process;

								String executionName = exProcessHandler.getExecutionName();

								if (executionName.contains("[watch]")) {
									process.destroyProcess();
								}
							}
						);
					}

				});
		}

		return runContentDescriptor;
	}

}