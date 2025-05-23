/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.server;

import com.intellij.build.BuildView;
import com.intellij.debugger.DebugEnvironment;
import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.DefaultDebugEnvironment;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.impl.GenericDebuggerRunnerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunnableState;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemTaskDebugRunner;
import com.intellij.openapi.project.Project;

import com.liferay.ide.idea.util.IntellijUtil;

import java.io.IOException;

import java.net.Socket;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Simon Jiang
 */
public class LiferayDockerServerDebuggerRunner extends ExternalSystemTaskDebugRunner implements ServerRunnerChecker {

	@Override
	public boolean canRun(@NotNull String executorId, @NotNull RunProfile runProfile) {
		if (DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) &&
			(runProfile instanceof LiferayDockerServerConfiguration)) {

			return true;
		}

		return false;
	}

	@NotNull
	@Override
	public String getRunnerId() {
		return "LiferayDockerServerDebuggerRunner";
	}

	@Override
	public boolean isLiferayServerRunConfiguration(RunConfiguration runConfiguration) {
		return runConfiguration instanceof LiferayDockerServerConfiguration;
	}

	@Nullable
	@Override
	protected RunContentDescriptor createContentDescriptor(
			@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment)
		throws ExecutionException {

		if (state instanceof ExternalSystemRunnableState) {
			RunnerSettings runnerSettings = environment.getRunnerSettings();

			GenericDebuggerRunnerSettings debuggerRunnerSettings = (GenericDebuggerRunnerSettings)runnerSettings;

			int port = Integer.decode(debuggerRunnerSettings.getDebugPort());

			if (port > 0) {
				ExternalSystemRunnableState myRunnableState = (ExternalSystemRunnableState)state;

				return _getRunContentDescriptor(myRunnableState, environment, port);
			}

			_logger.warn(
				"Can not attach debugger to external system task execution. Reason: target debug port is unknown");
		}
		else {
			Class<? extends RunProfileState> runProfileStateClass = state.getClass();

			_logger.warn(
				String.format(
					"Can not attach debugger to external system task execution. Reason: invalid run profile state is " +
						"provided expected '%s' but got '%s'",
					ExternalSystemRunnableState.class.getName(), runProfileStateClass.getName()));
		}

		return null;
	}

	@Nullable
	@Override
	protected RunContentDescriptor doExecute(
			@NotNull RunProfileState runProfileState, @NotNull ExecutionEnvironment executionEnvironment)
		throws ExecutionException {

		RunnerSettings runnerSettings = executionEnvironment.getRunnerSettings();

		GenericDebuggerRunnerSettings debuggerRunnerSettings = (GenericDebuggerRunnerSettings)runnerSettings;

		debuggerRunnerSettings.setDebugPort("8000");

		RunContentDescriptor runContentDescriptor = super.doExecute(runProfileState, executionEnvironment);

		if (runContentDescriptor != null) {
			ProcessHandler processHandler = runContentDescriptor.getProcessHandler();

			if (processHandler == null) {
				return runContentDescriptor;
			}

			IntellijUtil.registerDockerSeverStopHandler(processHandler, runProfileState, executionEnvironment);
		}

		return runContentDescriptor;
	}

	private boolean _canConnectToRemoteSocket(String host, int port) {
		try {
			Socket socket = new Socket(host, port);

			socket.close();

			return true;
		}
		catch (IOException ioException) {
			return false;
		}
	}

	@Nullable
	private RunContentDescriptor _getRunContentDescriptor(
			@NotNull ExternalSystemRunnableState state, @NotNull ExecutionEnvironment environment, int port)
		throws ExecutionException {

		RemoteConnection connection = new RemoteConnection(true, "127.0.0.1", String.valueOf(port), false);

		RunContentDescriptor runContentDescriptor = attachVirtualMachine(state, environment, connection, true);

		if (runContentDescriptor == null) {
			return null;
		}

		Application application = ApplicationManager.getApplication();

		application.executeOnPooledThread(
			() -> {
				try {
					boolean debugPortStarted = false;
					String host = "127.0.0.1";

					do {
						boolean canConnect = _canConnectToRemoteSocket(host, port);

						try {
							if (canConnect) {
								debugPortStarted = true;

								Project project = environment.getProject();

								DebuggerManager debuggerManagerInstance = DebuggerManager.getInstance(project);

								DebugProcess debugProcess = debuggerManagerInstance.getDebugProcess(
									runContentDescriptor.getProcessHandler());

								DebuggerManagerEx debuggerManagerExInstance = DebuggerManagerEx.getInstanceEx(project);

								Collection<DebuggerSession> sessions = debuggerManagerExInstance.getSessions();

								DebuggerSession debuggerSession = sessions.stream(
								).filter(
									session -> debugProcess == session.getProcess()
								).findFirst(
								).orElse(
									null
								);

								DebugEnvironment debugEnvironment = new DefaultDebugEnvironment(
									environment, state, connection, DebugEnvironment.LOCAL_START_TIMEOUT);

								DebugProcessImpl debugProcessImpl = (DebugProcessImpl)debugProcess;

								debugProcessImpl.attachVirtualMachine(debugEnvironment, debuggerSession);
							}

							Thread.sleep(500);
						}
						catch (Exception exception) {
						}
					}
					while (!debugPortStarted);
				}
				catch (Exception exception) {
					_logger.warn(exception);
				}
			});

		state.setContentDescriptor(runContentDescriptor);

		ExecutionConsole executionConsole = runContentDescriptor.getExecutionConsole();

		if (executionConsole instanceof BuildView) {
			return runContentDescriptor;
		}

		RunContentDescriptor descriptor = new RunContentDescriptor(
			runContentDescriptor.getExecutionConsole(), runContentDescriptor.getProcessHandler(),
			runContentDescriptor.getComponent(), runContentDescriptor.getDisplayName(), runContentDescriptor.getIcon(),
			null, runContentDescriptor.getRestartActions());

		descriptor.setRunnerLayoutUi(runContentDescriptor.getRunnerLayoutUi());

		return descriptor;
	}

	private static final Logger _logger = Logger.getInstance(LiferayDockerServerDebuggerRunner.class);

}