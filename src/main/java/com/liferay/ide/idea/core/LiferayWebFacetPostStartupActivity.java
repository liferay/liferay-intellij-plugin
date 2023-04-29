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

package com.liferay.ide.idea.core;

import com.intellij.ProjectTopics;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.facet.ProjectFacetManager;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.javaee.web.facet.WebFacetConfiguration;
import com.intellij.javaee.web.facet.WebFacetType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.util.List;
import java.util.stream.Stream;

import kotlin.Unit;

import kotlin.coroutines.Continuation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayWebFacetPostStartupActivity implements DumbAware, LiferayWorkspaceSupport, ProjectActivity {

	@Nullable
	@Override
	public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
		VirtualFile projectDirVirtualFile = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (projectDirVirtualFile == null) {
			return project;
		}

		projectDirVirtualFile.refresh(false, true);

		MessageBus messageBus = project.getMessageBus();

		MessageBusConnection messageBusConnection = messageBus.connect(project);

		messageBusConnection.subscribe(
			ProjectTopics.MODULES,
			new ModuleListener() {

				@Override
				public void modulesAdded(@NotNull Project project, @NotNull List<? extends Module> modules) {
					Application application = ApplicationManager.getApplication();

					application.runWriteAction(
						() -> {
							if (LiferayWorkspaceSupport.isValidWorkspaceLocation(project)) {
								for (Module module : modules) {
									_addWebRoot(module);
								}
							}
						});
				}

			});

		return project;
	}

	private void _addWebRoot(Module module) {
		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		Stream.of(
			moduleRootManager.getSourceRoots()
		).filter(
			sourceRoot -> {
				String sourcePath = sourceRoot.getPath();

				return sourcePath.contains("src/main/resources");
			}
		).map(
			sourceRoot -> {
				String sourcePath = sourceRoot.getPath();

				String resourcesPath = sourcePath.concat("/META-INF/resources");

				LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

				return localFileSystem.findFileByPath(resourcesPath);
			}
		).filter(
			resourcesFolder -> FileUtil.exists(resourcesFolder)
		).forEach(
			resourcesFolder -> {
				boolean hasWebFacet = false;

				FacetManager facetManager = FacetManager.getInstance(module);

				Facet<?>[] facets = facetManager.getAllFacets();

				for (Facet<?> facet : facets) {
					WebFacetType webFacetType = WebFacetType.getInstance();

					FacetType<?, ?> facetType = facet.getType();

					String facetTypePresentableName = facetType.getPresentableName();

					if (facetTypePresentableName.equals(webFacetType.getPresentableName())) {
						hasWebFacet = true;

						break;
					}
				}

				if (!hasWebFacet) {
					ProjectFacetManager projectFacetManager = ProjectFacetManager.getInstance(module.getProject());

					WebFacetConfiguration webFacetConfiguration = projectFacetManager.createDefaultConfiguration(
						WebFacetType.getInstance());

					ModifiableFacetModel modifiableFacetModel = facetManager.createModifiableModel();

					WebFacetType webFacetType = WebFacetType.getInstance();

					WebFacet webFacet = facetManager.createFacet(
						webFacetType, webFacetType.getPresentableName(), webFacetConfiguration, null);

					webFacet.addWebRoot(resourcesFolder, "/");

					modifiableFacetModel.addFacet(webFacet);

					Application application = ApplicationManager.getApplication();

					application.invokeLater(() -> application.runWriteAction(modifiableFacetModel::commit));
				}
			}
		);
	}

}