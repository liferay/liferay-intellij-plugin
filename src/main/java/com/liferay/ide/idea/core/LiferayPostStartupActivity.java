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
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ProjectConfigurationUtil;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenImportListener;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

/**
 * @author Simon Jiang
 */
public class LiferayPostStartupActivity implements DumbAware, LiferayWorkspaceSupport, StartupActivity.Background {

	@Override
	public void runActivity(@NotNull Project project) {
		VirtualFile projectDirVirtualFile = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (projectDirVirtualFile == null) {
			return;
		}

		projectDirVirtualFile.refresh(false, true);

		MessageBus messageBus = project.getMessageBus();

		MessageBusConnection messageBusConnection = messageBus.connect(project);

		StartupManager startupManager = StartupManager.getInstance(project);

		startupManager.runWhenProjectIsInitialized(
			() -> messageBusConnection.subscribe(
				ProjectDataImportListener.TOPIC,
				projectPath -> {
					Application application = ApplicationManager.getApplication();

					application.runReadAction(
						() -> {
							if (projectPath.equals(project.getBasePath())) {
								String homeDir = getHomeDir(project.getBasePath());

								ProjectConfigurationUtil.configExcludedFolder(project, homeDir);
							}
						});
				}));

		startupManager.registerPostStartupActivity(
			() -> messageBusConnection.subscribe(
				ProjectTopics.MODULES,
				new ModuleListener() {

					@Override
					public void moduleAdded(@NotNull Project project, @NotNull Module module) {
						Application application = ApplicationManager.getApplication();

						application.runWriteAction(
							() -> {
								if (LiferayWorkspaceSupport.isValidWorkspaceLocation(project)) {
									_addWebRoot(module);
								}
							});
					}

				}));

		messageBusConnection.subscribe(
			MavenImportListener.TOPIC,
			(projects, list) -> {
				Stream<Module> modulesStream = list.stream();

				modulesStream.map(
					module -> module.getProject()
				).filter(
					moduleProject -> moduleProject.equals(project)
				).distinct(
				).forEach(
					moduleProject -> {
						MavenProjectsManager mvnManager = MavenProjectsManager.getInstance(project);

						mvnManager.forceUpdateAllProjectsOrFindAllAvailablePomFiles();

						Application application = ApplicationManager.getApplication();

						application.runReadAction(
							() -> {
								String homeDir = getMavenProperty(
									moduleProject, WorkspaceConstants.MAVEN_HOME_DIR_PROPERTY,
									WorkspaceConstants.HOME_DIR_DEFAULT);

								ProjectConfigurationUtil.configExcludedFolder(moduleProject, homeDir);
							});
					}
				);
			});
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
			resourcesFolder -> FileUtil.exist(resourcesFolder)
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