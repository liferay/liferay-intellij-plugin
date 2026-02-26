/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.server;

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemProcessHandler;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class LiferayServerDebuggerRunner extends GenericDebuggerRunner implements ServerRunnerChecker {

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
		return "LiferayServerDebuggerRunner";
	}

	@Override
	public boolean isLiferayServerRunConfiguration(RunConfiguration runConfiguration) {
		return runConfiguration instanceof LiferayServerConfiguration;
	}

	@Nullable
	@Override
	protected RunContentDescriptor doExecute(
			@NotNull RunProfileState runProfileState, @NotNull ExecutionEnvironment executionEnvironment)
		throws ExecutionException {

		verifyRunningServer(executionEnvironment);

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
				new ProcessListener() {

					@Override
					public void processTerminated(@NotNull ProcessEvent event) {
						processHandler.removeProcessListener(this);
					}

					@Override
					public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed) {
						ExecutionManager executionManager = ExecutionManager.getInstance(
							executionEnvironment.getProject());

						Stream.of(
							executionManager.getRunningProcesses()
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

			ProcessTerminatedListener.attach(
				processHandler, executionEnvironment.getProject(), "Portal Bundle Stopped");
		}

		return runContentDescriptor;
	}

}