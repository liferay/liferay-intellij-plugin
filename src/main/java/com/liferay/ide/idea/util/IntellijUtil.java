/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemProcessHandler;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunnableState;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemNotificationManager;
import com.intellij.openapi.externalSystem.service.notification.NotificationCategory;
import com.intellij.openapi.externalSystem.service.notification.NotificationData;
import com.intellij.openapi.externalSystem.service.notification.NotificationSource;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.execution.ParametersListUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Charles Wu
 */
public class IntellijUtil {

	public static void cleanDockerContainerAndImage(Project project) {
		try {
			ExternalSystemTaskExecutionSettings settings = new ExternalSystemTaskExecutionSettings();

			CommandLineParser gradleCmdParser = new CommandLineParser();

			ParsedCommandLine parsedCommandLine = gradleCmdParser.parse(
				ParametersListUtil.parse("removeDockerContainer cleanDockerImage", true));

			settings.setExternalProjectPath(project.getBasePath());
			settings.setExternalSystemIdString(GradleConstants.SYSTEM_ID.toString());

			settings.setTaskNames(parsedCommandLine.getExtraArguments());

			ExternalSystemUtil.runTask(settings, DefaultRunExecutor.EXECUTOR_ID, project, GradleConstants.SYSTEM_ID);
		}
		catch (Exception exception) {
			NotificationData notificationData = new NotificationData(
				"<b>Failed to execute remove and clean container image tasks</b>", "<i>" + exception.getMessage(),
				NotificationCategory.ERROR, NotificationSource.TASK_EXECUTION);

			notificationData.setBalloonNotification(true);

			ExternalSystemNotificationManager externalSystemNotificationManager =
				ExternalSystemNotificationManager.getInstance(project);

			externalSystemNotificationManager.showNotification(GradleConstants.SYSTEM_ID, notificationData);
		}
	}

	public static VirtualFile getChild(VirtualFile parent, String name) {
		if (parent == null) {
			return null;
		}

		int index = name.indexOf('/');

		String pathElement = name;

		if (index > -1) {
			pathElement = name.substring(0, index);
		}

		for (VirtualFile virtualFile : parent.getChildren()) {
			if (pathElement.equals(virtualFile.getName())) {
				if (index == -1) {
					return virtualFile;
				}

				return getChild(virtualFile, name.substring(index + 1));
			}
		}

		return null;
	}

	public static VirtualFile getJarRoot(VirtualFile virtualFile) {
		VirtualFile jarRoot;

		VirtualFileSystem virtualFileSystem = virtualFile.getFileSystem();

		if (virtualFileSystem instanceof JarFileSystem) {
			JarFileSystem jarFileSystem = (JarFileSystem)virtualFileSystem;

			jarRoot = jarFileSystem.getRootByEntry(virtualFile);
		}
		else {
			JarFileSystem jarFileSystem = JarFileSystem.getInstance();

			jarRoot = jarFileSystem.getJarRootForLocalFile(virtualFile);
		}

		return jarRoot;
	}

	public static Module getModule(Project project, String moduleName) {
		Module module = null;

		final ProjectStructureConfigurable fromConfigurable = ProjectStructureConfigurable.getInstance(project);

		if (fromConfigurable != null) {
			ModuleStructureConfigurable modulesConfig = fromConfigurable.getModulesConfig();

			module = modulesConfig.getModule(moduleName);
		}
		else {
			ModuleManager moduleManager = ModuleManager.getInstance(project);

			module = moduleManager.findModuleByName(moduleName);
		}

		if (Objects.isNull(module)) {
			ModuleManager moduleManager = ModuleManager.getInstance(project);

			Module[] modules = moduleManager.getModules();

			for (Module moduleItem : modules) {
				String moduleItemName = moduleItem.getName();

				if (moduleItemName.equals(moduleName)) {
					return moduleItem;
				}
			}
		}

		return module;
	}

	@Nullable
	public static PsiFile getModulePsiFileByName(@NotNull Module module, String fileName) {
		PsiFile[] psiFiles = getModulePsiFilesByName(module, fileName);

		if (psiFiles.length > 0) {
			assert psiFiles.length < 2;

			return psiFiles[0];
		}

		return null;
	}

	public static PsiFile[] getModulePsiFilesByName(@NotNull Module module, String name) {
		return Stream.of(
			getProjectPsiFilesByName(module.getProject(), name)
		).filter(
			psiFile -> ModuleUtil.findModuleForFile(psiFile) == module
		).toArray(
			PsiFile[]::new
		);
	}

	@Nullable
	public static Module getParentModule(Module module) {
		ModuleManager moduleManager = ModuleManager.getInstance(module.getProject());

		ModifiableModuleModel modifiableModuleModel = moduleManager.getModifiableModel();

		String[] groupPaths = modifiableModuleModel.getModuleGroupPath(module);

		String path = ArrayUtil.getLastElement(groupPaths);

		if (path != null) {
			for (Module modifiableModule : modifiableModuleModel.getModules()) {
				if (path.equals(modifiableModule.getName())) {
					return modifiableModule;
				}
			}
		}

		return null;
	}

	public static PsiFile[] getProjectPsiFilesByName(Project project, String name) {
		Collection<VirtualFile> virtualFiles = FilenameIndex.getVirtualFilesByName(
			name, GlobalSearchScope.projectScope(project));

		PsiManager psiManager = PsiManager.getInstance(project);

		List<PsiFile> psiFiles = new ArrayList<>();

		for (VirtualFile virtualFile : virtualFiles) {
			psiFiles.add(psiManager.findFile(virtualFile));
		}

		return psiFiles.toArray(new PsiFile[0]);
	}

	public static void registerDockerSeverStopHandler(
		ProcessHandler processHandler, @NotNull RunProfileState runProfileState,
		@NotNull ExecutionEnvironment environment) {

		Application application = ApplicationManager.getApplication();

		application.invokeLater(
			() -> {
				try {
					if (processHandler instanceof ExternalSystemProcessHandler) {
						ExternalSystemProcessHandler exProcessHandler = (ExternalSystemProcessHandler)processHandler;

						exProcessHandler.addProcessListener(
							new ProcessAdapter() {

								@Override
								public void processTerminated(@NotNull ProcessEvent event) {
									ProcessHandler handler = event.getProcessHandler();

									ExternalSystemProcessHandler exHandler = (ExternalSystemProcessHandler)handler;

									String executionName = exHandler.getExecutionName();

									if (executionName.contains("Liferay Docker") &&
										(runProfileState instanceof ExternalSystemRunnableState)) {

										Project project = environment.getProject();

										ExternalSystemTaskExecutionSettings settings =
											new ExternalSystemTaskExecutionSettings();

										CommandLineParser gradleCmdParser = new CommandLineParser();

										ParsedCommandLine parsedCommandLine = gradleCmdParser.parse(
											ParametersListUtil.parse("stopDockerContainer", true));

										settings.setExternalProjectPath(project.getBasePath());
										settings.setExternalSystemIdString(GradleConstants.SYSTEM_ID.toString());
										settings.setTaskNames(parsedCommandLine.getExtraArguments());

										ExternalSystemUtil.runTask(
											settings, DefaultRunExecutor.EXECUTOR_ID, project,
											GradleConstants.SYSTEM_ID,
											new TaskCallback() {

												@Override
												public void onFailure() {
													IntellijUtil.cleanDockerContainerAndImage(project);
												}

												@Override
												public void onSuccess() {
													IntellijUtil.cleanDockerContainerAndImage(project);
												}

											},
											ProgressExecutionMode.IN_BACKGROUND_ASYNC, true);
									}
								}

							});
					}
				}
				catch (Exception exception) {
				}
			});

		processHandler.addProcessListener(
			new ProcessAdapter() {

				@Override
				public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed) {
					if (processHandler.equals(event.getProcessHandler()) &&
						(processHandler instanceof ExternalSystemProcessHandler)) {

						ExternalSystemProcessHandler exProcessHandler = (ExternalSystemProcessHandler)processHandler;

						String executionName = exProcessHandler.getExecutionName();

						if (executionName.contains("Liferay Docker")) {
							Project environmentProject = environment.getProject();

							DebuggerManager debuggerManagerInstance = DebuggerManager.getInstance(environmentProject);

							DebugProcess debugProcess = debuggerManagerInstance.getDebugProcess(processHandler);

							if (debugProcess != null) {
								debugProcess.stop(true);
							}

							exProcessHandler.detachProcess();

							exProcessHandler.destroyProcess();
						}
					}
				}

			});
	}

	public static boolean validateExistingModuleName(String moduleName) {
		if (Objects.isNull(moduleName)) {
			return false;
		}

		return moduleName.matches(_MODULE_NAME_REGEX);
	}

	private static final String _MODULE_NAME_REGEX = "([A-Za-z0-9_\\-.]+[A-Za-z0-9]$)|([A-Za-z0-9])";

}