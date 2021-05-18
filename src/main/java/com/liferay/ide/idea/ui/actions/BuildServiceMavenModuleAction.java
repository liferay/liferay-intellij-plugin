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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ListUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * @author Ethan Sun
 */
public class BuildServiceMavenModuleAction extends AbstractLiferayMavenGoalAction implements LiferayWorkspaceSupport {

	public BuildServiceMavenModuleAction() {
		super("BuildService", "Run buildService goal", LiferayIcons.LIFERAY_ICON);

		goals = Arrays.asList("service-builder:build");
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		if (super.isEnabledAndVisible(anActionEvent)) {
			_selectedFile = getVirtualFile(anActionEvent);

			return ListUtil.isNotEmpty(_getServiceBuilderModules(_selectedFile));
		}

		return false;
	}

	private Set<Module> _getServiceBuilderModules(VirtualFile virtualFile) {
		Set<Module> result = new HashSet<>();

		VfsUtil.visitChildrenRecursively(
			virtualFile,
			new VirtualFileVisitor<Void>(VirtualFileVisitor.NO_FOLLOW_SYMLINKS, VirtualFileVisitor.limit(5)) {

				@NotNull
				@Override
				public Result visitFileEx(@NotNull VirtualFile file) {
					if (!file.isDirectory() && Objects.equals(file.getName(), "service.xml")) {
						Module moduleForFile = ModuleUtil.findModuleForFile(file, project);

						if (Objects.nonNull(moduleForFile)) {
							result.add(moduleForFile);
						}

						return SKIP_CHILDREN;
					}

					Module moduleForFile = ModuleUtil.findModuleForFile(file, project);

					if (Objects.isNull(moduleForFile)) {
						return SKIP_CHILDREN;
					}

					boolean moduleDir = ModuleUtil.isModuleDir(moduleForFile, file);

					if (moduleDir && Objects.nonNull(file.findChild("service.xml"))) {
						result.add(moduleForFile);

						return SKIP_CHILDREN;
					}

					return CONTINUE;
				}

			});

		return result;
	}

	private VirtualFile _selectedFile;

}