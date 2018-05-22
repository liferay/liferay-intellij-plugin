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

package com.liferay.ide.idea.project;

import com.intellij.ProjectTopics;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.javaee.web.facet.WebFacetType;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import com.liferay.ide.idea.util.FileUtil;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 * @autor Joye Luo
 */
public class LiferayProjectComponent extends AbstractProjectComponent {

	public LiferayProjectComponent(Project project) {
		super(project);
	}

	@Override
	public void disposeComponent() {
		_messageBusConnection.disconnect();

		super.disposeComponent();
	}

	@Override
	public void initComponent() {
		super.initComponent();

		MessageBus messageBus = myProject.getMessageBus();

		_messageBusConnection = messageBus.connect();

		ModuleListener moduleListener = new ModuleListener() {

			@Override
			public void moduleAdded(@NotNull Project project, @NotNull Module module) {
				_addWebRoot(module);
			}

		};

		_messageBusConnection.subscribe(ProjectTopics.MODULES, moduleListener);
	}

	private void _addWebRoot(Module module) {
		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		VirtualFile[] sourceRoots = moduleRootManager.getSourceRoots();

		if (sourceRoots.length > 0) {
			for (VirtualFile sourceRoot : sourceRoots) {
				String sourcePath = sourceRoot.getPath();

				if (sourcePath.contains("src/main/resources")) {
					String resourcesPath = sourcePath.concat("/META-INF/resources");

					LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

					VirtualFile resources = localFileSystem.findFileByPath(resourcesPath);

					if (FileUtil.exist(resources)) {
						boolean hasWebFacet = false;

						FacetManager facetManager = FacetManager.getInstance(module);

						Facet[] facets = facetManager.getAllFacets();

						for (Facet facet : facets) {
							WebFacetType webFacetType = WebFacetType.getInstance();

							FacetType facetType = facet.getType();

							String facetTypePresentableName = facetType.getPresentableName();

							if (facetTypePresentableName.equals(webFacetType.getPresentableName())) {
								hasWebFacet = true;

								break;
							}
						}

						if (!hasWebFacet) {
							WebFacet webFacet = FacetUtil.addFacet(module, WebFacetType.getInstance());

							webFacet.addWebRoot(resources, "/");

							myProject.save();
						}
					}
				}
			}
		}
	}

	private MessageBusConnection _messageBusConnection;

}