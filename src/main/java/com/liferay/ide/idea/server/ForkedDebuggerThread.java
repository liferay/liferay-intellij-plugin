// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.liferay.ide.idea.server;

import com.intellij.debugger.DebuggerBundle;
import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.JavaDebuggerBundle;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.jdi.VirtualMachineProxy;
import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.externalSystem.util.ExternalSystemBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.ui.UIUtil;

import java.awt.Font;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Soroka
 */
public class ForkedDebuggerThread extends Thread {

	@Override
	public void run() {
		while (!myMainProcessHandler.isProcessTerminated() && !myMainProcessHandler.isProcessTerminating() &&
			   !mySocket.isClosed()) {

			try {
				handleForkedProcessSignal(mySocket.accept());
			}
			catch (EOFException eofe) {
			}
			catch (IOException ioe) {
			}
		}

		try {
			if (!mySocket.isClosed()) {
				mySocket.close();
			}
		}
		catch (IOException ioe) {
			ForkedDebuggerThread.LOG.debug(ioe);
		}
	}

	private static void attachVM(
		@NotNull Project project, String runConfigName, @NotNull String debugPort, ProgramRunner.Callback callback) {

		RunnerAndConfigurationSettings runSettings = RunManager.getInstance(
			project
		).createConfiguration(
			runConfigName, RemoteConfigurationType.class
		);

		runSettings.setActivateToolWindowBeforeRun(false);

		RemoteConfiguration configuration = (RemoteConfiguration)runSettings.getConfiguration();

		configuration.HOST = "localhost";
		configuration.PORT = debugPort;
		configuration.USE_SOCKET_TRANSPORT = true;
		configuration.SERVER_MODE = true;

		try {
			ExecutionEnvironment environment = ExecutionEnvironmentBuilder.create(
				DefaultDebugExecutor.getDebugExecutorInstance(), runSettings
			).contentToReuse(
				null
			).dataContext(
				null
			).activeTarget(
			).build();

			ProgramRunnerUtil.executeConfigurationAsync(environment, true, true, callback);
		}
		catch (ExecutionException ee) {
			ForkedDebuggerThread.LOG.error(ee);
		}
	}

	private void handleForkedProcessSignal(Socket accept) throws IOException {

		// the stream can not be closed in the current thread

		//noinspection IOResourceOpenedButNotSafelyClosed
		DataInputStream stream = new DataInputStream(accept.getInputStream());
		myMainProcessHandler.addProcessListener(
			new ProcessAdapter() {

				@Override
				public void processTerminated(@NotNull ProcessEvent event) {
					StreamUtil.closeStream(stream);
					StreamUtil.closeStream(accept);
				}

			});
		int signal = stream.readInt();
		String processName = stream.readUTF();

		if (signal > 0) {
			String debugPort = String.valueOf(signal);
			attachVM(
				myProject, processName, debugPort,
				new ProgramRunner.Callback() {

					@Override
					public void processStarted(RunContentDescriptor descriptor) {

						// select tab for the forked process only when it has been suspended

						descriptor.setSelectContentWhenAdded(false);

						// restore selection of the 'main' tab to avoid flickering of the reused content tab when no suspend events occur

						ProcessHandler forkedProcessHandler = descriptor.getProcessHandler();

						if (forkedProcessHandler != null) {
							myMainProcessHandler.addProcessListener(
								new ProcessAdapter() {

									@Override
									public void processWillTerminate(
										@NotNull ProcessEvent event, boolean willBeDestroyed) {

										myMainProcessHandler.removeProcessListener(this);
										terminateForkedProcess(forkedProcessHandler);
									}

								});

							forkedProcessHandler.addProcessListener(
								new ForkedDebuggerThread.MyForkedProcessListener(descriptor, processName));

							try {
								accept.getOutputStream(
								).write(
									0
								);
								stream.close();
							}
							catch (IOException ioe) {
								ForkedDebuggerThread.LOG.debug(ioe);
							}
						}
					}

				});
		}
		else if (signal == 0) {

			// remove content for terminated forked processes

			ApplicationManager.getApplication(
			).invokeLater(
				() -> {
					final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
					ContentManager contentManager = toolWindowManager.getToolWindow(
						ToolWindowId.DEBUG
					).getContentManager();

					Content content = contentManager.findContent(processName);

					if (content != null) {
						RunContentDescriptor descriptor = content.getUserData(RunContentDescriptor.DESCRIPTOR_KEY);

						if (descriptor != null) {
							ProcessHandler handler = descriptor.getProcessHandler();

							if (handler != null) {
								handler.destroyProcess();
							}
						}
					}

					try {
						accept.getOutputStream(
						).write(
							0
						);
						stream.close();
					}
					catch (IOException ioe) {
						ForkedDebuggerThread.LOG.debug(ioe);
					}
				}
			);
		}
	}

	private void terminateForkedProcess(@NotNull ProcessHandler processHandler) {
		DebugProcess debugProcess = DebuggerManager.getInstance(
			myProject
		).getDebugProcess(
			processHandler
		);

		if (debugProcess != null) {
			debugProcess.getManagerThread(
			).invokeCommand(
				new DebuggerCommand() {

					@Override
					public void action() {
						VirtualMachineProxy virtualMachineProxy = debugProcess.getVirtualMachineProxy();

						if ((virtualMachineProxy instanceof VirtualMachineProxyImpl) &&
							((VirtualMachineProxyImpl)virtualMachineProxy).canBeModified()) {

							// use success exit code here to avoid the main process interruption

							((VirtualMachineProxyImpl)virtualMachineProxy).exit(0);
						}
						else {
							debugProcess.stop(true);
						}
					}

					@Override
					public void commandCancelled() {
						debugProcess.stop(true);
					}

				}
			);
		}
		else {
			processHandler.destroyProcess();
		}
	}

	private static final Logger LOG = Logger.getInstance(LiferayDockerServerConfiguration.class);

	ForkedDebuggerThread(
		@NotNull ProcessHandler mainProcessHandler, @NotNull RunContentDescriptor mainRunContentDescriptor,
		@NotNull ServerSocket socket, @NotNull Project project) {

		super("external task forked debugger runner");

		setDaemon(true);
		mySocket = socket;
		myProject = project;
		myMainProcessHandler = mainProcessHandler;
		myMainExecutionConsole = mainRunContentDescriptor.getExecutionConsole();
		myMainProcessHandler.addProcessListener(
			new ProcessAdapter() {

				@Override
				public void processTerminated(@NotNull ProcessEvent event) {
					closeSocket();
				}

				@Override
				public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed) {
					closeSocket();
				}

				void closeSocket() {
					try {
						if (!mySocket.isClosed()) {
							mySocket.close();
						}
					}
					catch (IOException ioe) {
					}
				}

			});
	}

	@Nullable
	private final ExecutionConsole myMainExecutionConsole;

	@NotNull
	private final ProcessHandler myMainProcessHandler;

	@NotNull
	private final Project myProject;

	@NotNull
	private final ServerSocket mySocket;

	private class MyForkedProcessListener extends ProcessAdapter {

		@Override
		public void processTerminated(@NotNull ProcessEvent event) {
			final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
			ContentManager contentManager = toolWindowManager.getToolWindow(
				ToolWindowId.DEBUG
			).getContentManager();
			Content content = myDescriptor.getAttachedContent();

			if (content != null) {
				ApplicationManager.getApplication(
				).invokeLater(
					() -> contentManager.removeContent(content, true)
				);
			}

			removeLink();
		}

		@Override
		public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed) {
			if (!willBeDestroyed) {

				// always terminate forked process

				terminateForkedProcess(event.getProcessHandler());
			}
		}

		@Override
		public void startNotified(@NotNull ProcessEvent event) {
			postLink();
		}

		MyForkedProcessListener(@NotNull RunContentDescriptor descriptor, @NotNull String processName) {
			myDescriptor = descriptor;
			myProcessName = processName;
		}

		@Nullable
		private ConsoleViewImpl getMainConsoleView() {
			if (myMainExecutionConsole == null)

				return null;

			if (myMainExecutionConsole instanceof ConsoleViewImpl) {
				return (ConsoleViewImpl)myMainExecutionConsole;
			}

			if (myMainExecutionConsole instanceof DataProvider) {
				Object consoleView = ((DataProvider)myMainExecutionConsole).getData(
					LangDataKeys.CONSOLE_VIEW.getName());

				if (consoleView instanceof ConsoleViewImpl) {
					return (ConsoleViewImpl)consoleView;
				}
			}

			return null;
		}

		private void postLink() {
			ConsoleViewImpl mainConsoleView = getMainConsoleView();

			if (mainConsoleView != null) {
				ProcessHandler handler = myDescriptor.getProcessHandler();
				String addressDisplayName = "";
				DebugProcess debugProcess = DebuggerManager.getInstance(
					myProject
				).getDebugProcess(
					handler
				);

				if (debugProcess instanceof DebugProcessImpl) {
					addressDisplayName =
						"(" + JavaDebuggerBundle.getAddressDisplayName(((DebugProcessImpl)debugProcess).getConnection()) +
							")";
				}

				String linkText = ExternalSystemBundle.message("debugger.open.session.tab");

				String debuggerAttachedStatusMessage =
					ExternalSystemBundle.message("debugger.status.connected", myProcessName, addressDisplayName) +
						". " + linkText + '\n';

				mainConsoleView.print(debuggerAttachedStatusMessage, ConsoleViewContentType.SYSTEM_OUTPUT);
				mainConsoleView.performWhenNoDeferredOutput(
					() -> {
						EditorHyperlinkSupport hyperlinkSupport = mainConsoleView.getHyperlinks();
						int messageOffset = mainConsoleView.getText(
						).indexOf(
							debuggerAttachedStatusMessage
						);
						int linkStartOffset = messageOffset + debuggerAttachedStatusMessage.indexOf(linkText);
						myHyperlink = hyperlinkSupport.createHyperlink(
							linkStartOffset, linkStartOffset + linkText.length(), null,
							project -> {

								// open tab

								final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
								ContentManager contentManager = toolWindowManager.getToolWindow(
									ToolWindowId.DEBUG
								).getContentManager();
								Content content = myDescriptor.getAttachedContent();

								if (content != null) {
									contentManager.setSelectedContent(content, true, true);
								}
							});
					});
			}
		}

		private void removeLink() {
			if (myHyperlink != null) {
				ConsoleViewImpl mainConsoleView = getMainConsoleView();

				if (mainConsoleView != null) {
					ApplicationManager.getApplication(
					).invokeLater(
						() -> {
							EditorHyperlinkSupport hyperlinkSupport = mainConsoleView.getHyperlinks();
							int startOffset = myHyperlink.getStartOffset();
							int endOffset = myHyperlink.getEndOffset();
							TextAttributes inactiveTextAttributes = myHyperlink.getTextAttributesKey().getDefaultAttributes() != null ?
							myHyperlink.getTextAttributesKey().getDefaultAttributes().clone() : TextAttributes.ERASE_MARKER.clone();

							inactiveTextAttributes.setForegroundColor(UIUtil.getInactiveTextColor());
							inactiveTextAttributes.setEffectColor(UIUtil.getInactiveTextColor());
							inactiveTextAttributes.setFontType(Font.ITALIC);
							hyperlinkSupport.removeHyperlink(myHyperlink);
							hyperlinkSupport.addHighlighter(
								startOffset, endOffset, inactiveTextAttributes, HighlighterLayer.CONSOLE_FILTER);
						}
					);
				}
			}
		}

		@NotNull
		private final RunContentDescriptor myDescriptor;

		@Nullable
		private RangeHighlighter myHyperlink;

		@NotNull
		private final String myProcessName;

	}

}