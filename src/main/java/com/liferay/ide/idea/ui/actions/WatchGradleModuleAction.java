/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.actions;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.server.gogo.GogoTelnetClient;
import com.liferay.ide.idea.server.portal.PortalBundle;
import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ServerUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class WatchGradleModuleAction extends AbstractLiferayGradleTaskAction {

	public WatchGradleModuleAction() {
		super("Watch", "Run watch task", LiferayIcons.LIFERAY_ICON, "watch");
	}

	@Override
	public void actionPerformed(final AnActionEvent event) {
		super.actionPerformed(event);
	}

	@Override
	protected void afterTask(Project project) {
		VirtualFile projectDir = ProjectUtil.guessProjectDir(project);

		List<Path> bndPaths = _getBndPaths(projectDir);

		if (bndPaths.isEmpty()) {
			return;
		}

		try (GogoTelnetClient gogoTelnetClient = new GogoTelnetClient("localhost", 11311)) {
			for (Path bndPath : bndPaths) {
				Properties properties = new Properties();

				try (InputStream inputStream = Files.newInputStream(bndPath)) {
					properties.load(inputStream);

					String bsn = properties.getProperty("Bundle-SymbolicName");

					String cmd = "uninstall " + bsn;

					gogoTelnetClient.send(cmd);
				}
				catch (IOException ioException) {
				}
			}
		}
		catch (IOException ioException) {
		}

		File file = new File(projectDir.getCanonicalPath());

		File installedBundleIdFile = new File(file, "build/installedBundleId");

		if (installedBundleIdFile.exists()) {
			installedBundleIdFile.delete();
		}
	}

	@Override
	protected boolean continuous() {
		return true;
	}

	@Override
	protected ProgressExecutionMode getProgressMode() {
		return ProgressExecutionMode.NO_PROGRESS_ASYNC;
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		if (super.isEnabledAndVisible(anActionEvent)) {
			Project project = anActionEvent.getProject();

			String homeDir = LiferayWorkspaceSupport.getHomeDir(project);

			Path bundlePath = Paths.get(homeDir);

			if (!bundlePath.isAbsolute()) {
				bundlePath = Paths.get(project.getBasePath(), homeDir);
			}

			PortalBundle portalBundle = ServerUtil.getPortalBundle(bundlePath);

			if (portalBundle == null) {
				return false;
			}

			VirtualFile virtualFile = getVirtualFile(anActionEvent);

			VirtualFile projectVirtualFile = ProjectUtil.guessProjectDir(project);

			Module module = ModuleUtil.findModuleForFile(virtualFile, project);

			if (projectVirtualFile.equals(virtualFile)) {
				return GradleUtil.isWatchableProject(module);
			}

			String moduleDirectoryName = LiferayWorkspaceSupport.getWorkspaceModuleDir(project);

			if (!Objects.isNull(moduleDirectoryName)) {
				String virtualFileToStr = virtualFile.toString();

				if (virtualFileToStr.contains("/" + moduleDirectoryName)) {
					return GradleUtil.isWatchableProject(module);
				}
			}
			else {
				return GradleUtil.isWatchableProject(module);
			}
		}

		return false;
	}

	@Override
	protected RunnerAndConfigurationSettings processRunnerConfiguration(AnActionEvent anActionEvent) {
		final RunnerAndConfigurationSettings runnerAndConfigurationSettings = super.processRunnerConfiguration(
			anActionEvent);

		if (runnerAndConfigurationSettings == null) {
			return null;
		}

		Project project = anActionEvent.getData(CommonDataKeys.PROJECT);

		if (project == null) {
			return null;
		}

		RunContentManager runContentManager = RunContentManager.getInstance(project);

		List<RunContentDescriptor> allDescriptors = runContentManager.getAllDescriptors();

		RunConfiguration configuration = runnerAndConfigurationSettings.getConfiguration();

		String configurationName = configuration.getName();

		final boolean watchWorkspaceProject = configurationName.equals(project.getName() + " [watch]");

		allDescriptors.forEach(
			descriptor -> {
				String displayName = descriptor.getDisplayName();

				ProcessHandler processHandler = descriptor.getProcessHandler();

				boolean processTerminated = processHandler.isProcessTerminated();

				if ((Objects.equals("Gradle." + displayName, runnerAndConfigurationSettings.getUniqueID()) &&
					 !processTerminated) ||
					(watchWorkspaceProject && displayName.contains("[watch]"))) {

					processHandler.destroyProcess();

					Content content = descriptor.getAttachedContent();

					ContentManager contentManager = content.getManager();

					contentManager.removeContent(descriptor.getAttachedContent(), true);
				}
			});

		return runnerAndConfigurationSettings;
	}

	private List<Path> _getBndPaths(VirtualFile projectDir) {
		File file = new File(projectDir.getCanonicalPath());

		File bndFile = new File(file, "bnd.bnd");

		List<Path> bndFiles = new ArrayList<>();

		if (!bndFile.exists()) {
			try {
				Files.walkFileTree(
					Paths.get(file.getPath()),
					new SimpleFileVisitor<Path>() {

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException ioException) {
							if (FileUtil.exists(new File(dir.toFile(), "bnd.bnd"))) {
								return FileVisitResult.SKIP_SUBTREE;
							}

							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
							if (file.endsWith("bnd.bnd")) {
								bndFiles.add(file);

								return FileVisitResult.SKIP_SIBLINGS;
							}

							return FileVisitResult.CONTINUE;
						}

					});
			}
			catch (IOException ioException) {
			}
		}
		else {
			bndFiles.add(bndFile.toPath());
		}

		return bndFiles;
	}

}