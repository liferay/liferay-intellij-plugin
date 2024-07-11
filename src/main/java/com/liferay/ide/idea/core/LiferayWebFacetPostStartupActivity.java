/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.core;

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
public class LiferayWebFacetPostStartupActivity implements DumbAware, ProjectActivity {

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
			ModuleListener.TOPIC,
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