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

package com.liferay.ide.idea.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.server.gogo.GogoTelnetClient;
import com.liferay.ide.idea.util.GradleUtil;

import icons.LiferayIcons;

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
	protected void afterTask(VirtualFile projectDir) {
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
				catch (IOException ioe) {
				}
			}
		}
		catch (IOException ioe) {
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
		Project project = anActionEvent.getProject();

		VirtualFile virtualFile = getVirtualFile(anActionEvent);

		if ((project == null) || (virtualFile == null)) {
			return false;
		}

		Module module = ModuleUtil.findModuleForFile(virtualFile, project);

		if (module == null) {
			return false;
		}

		return GradleUtil.isWatchableProject(module);
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
						public FileVisitResult postVisitDirectory(Path dir, IOException e) {
							if (new File(dir.toFile(), "bnd.bnd").exists()) {
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
			catch (IOException ioe) {
			}
		}
		else {
			bndFiles.add(bndFile.toPath());
		}

		return bndFiles;
	}

}