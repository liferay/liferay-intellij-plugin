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

import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.server.gogo.GogoTelnetClient;
import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.LiferayIcons;

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
 */
public class WatchGradleModuleAction extends AbstractLiferayGradleTaskAction {

	public WatchGradleModuleAction() {
		super("Watch", "Run watch task", LiferayIcons.LIFERAY_ICON, "watch");
	}

	@Override
	public void afterTask(AnActionEvent event) {
		List<Path> bndPaths = _getBndPaths(event);

		if (bndPaths.isEmpty()) {
			return;
		}

		try (GogoTelnetClient client = new GogoTelnetClient("localhost", 11311)) {
			for (Path bndPath : bndPaths) {
				Properties properties = new Properties();

				try (InputStream in = Files.newInputStream(bndPath)) {
					properties.load(in);

					String bsn = properties.getProperty("Bundle-SymbolicName");

					String cmd = "uninstall " + bsn;

					client.send(cmd);
				}
				catch (IOException ioe) {
				}
			}
		}
		catch (IOException ioe) {
		}
	}

	@Override
	public boolean continuous() {
		return true;
	}

	@Override
	public boolean isEnabledAndVisible(AnActionEvent event) {
		Project project = event.getProject();
		VirtualFile file = getVirtualFile(event);

		VirtualFile baseDir = project.getBaseDir();

		VirtualFile gradleFile = baseDir.findChild("settings.gradle");

		if ((file != null) && (gradleFile != null) && ProjectRootsUtil.isModuleContentRoot(file, project)) {
			File settingsFile = new File(gradleFile.getPath());

			if (GradleUtil.isWatchableProject(settingsFile)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected String getWorkingDirectory(AnActionEvent event) {
		VirtualFile virtualFile = getVirtualFile(event);

		ProjectRootManager projectRootManager = ProjectRootManager.getInstance(event.getProject());

		ProjectFileIndex projectFileIndex = projectRootManager.getFileIndex();

		Module module = projectFileIndex.getModuleForFile(virtualFile);

		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		ModifiableRootModel modifiableRootModel = moduleRootManager.getModifiableModel();

		VirtualFile[] roots = modifiableRootModel.getContentRoots();

		assert roots != null && roots[0] != null;

		return roots[0].getCanonicalPath();
	}

	private List<Path> _getBndPaths(AnActionEvent event) {
		File file = new File(getWorkingDirectory(event));

		File bndFile = new File(file, "bnd.bnd");

		List<Path> bndFiles = new ArrayList<>();

		if (!bndFile.exists()) {
			try {
				Files.walkFileTree(
					Paths.get(file.getPath()),
					new SimpleFileVisitor<Path>() {

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
							if (new File(dir.toFile(), "bnd.bnd").exists()) {
								return FileVisitResult.SKIP_SUBTREE;
							}

							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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