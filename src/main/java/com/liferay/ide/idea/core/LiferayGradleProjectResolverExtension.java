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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.project.IExternalSystemSourceType;
import com.intellij.openapi.externalSystem.model.project.ModuleData;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.util.Pair;

import java.io.File;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.idea.IdeaDependency;
import org.gradle.tooling.model.idea.IdeaModule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.model.DefaultExternalLibraryDependency;
import org.jetbrains.plugins.gradle.model.ExternalDependency;
import org.jetbrains.plugins.gradle.model.ExternalProject;
import org.jetbrains.plugins.gradle.model.ExternalSourceDirectorySet;
import org.jetbrains.plugins.gradle.model.ExternalSourceSet;
import org.jetbrains.plugins.gradle.model.data.GradleSourceSetData;
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension;
import org.jetbrains.plugins.gradle.service.project.GradleProjectResolver;
import org.jetbrains.plugins.gradle.service.project.GradleProjectResolverUtil;
import org.jetbrains.plugins.gradle.service.project.ProjectResolverContext;
import org.jetbrains.plugins.gradle.tooling.serialization.internal.adapter.InternalIdeaDependencyScope;
import org.jetbrains.plugins.gradle.tooling.serialization.internal.adapter.InternalIdeaSingleEntryLibraryDependency;

/**
 * @author Simon Jiang
 * @author Ethan Sun
 */
public class LiferayGradleProjectResolverExtension extends AbstractProjectResolverExtension {

	@Override
	public void populateModuleDependencies(
		@NotNull IdeaModule gradleModule, @NotNull DataNode<ModuleData> ideModule,
		@NotNull final DataNode<ProjectData> ideProject) {

		ExternalProject externalProject = _getExternalProject(gradleModule, resolverCtx);

		if (resolverCtx.isResolveModulePerSourceSet()) {
			final Map<String, Pair<DataNode<GradleSourceSetData>, ExternalSourceSet>> sourceSetMap =
				ideProject.getUserData(GradleProjectResolver.RESOLVED_SOURCE_SETS);
			final Map<String, String> artifactsMap = ideProject.getUserData(
				GradleProjectResolver.CONFIGURATION_ARTIFACTS);
            assert sourceSetMap != null;
            assert artifactsMap != null;
            assert externalProject != null;
			_processSourceSets(
				resolverCtx, gradleModule, externalProject, ideModule,
				new SourceSetsProcessor() {

					@Override
					public void process(
						@NotNull IdeaModule gradleModule, @NotNull DataNode<? extends ModuleData> dataNode,
						@NotNull ExternalSourceSet sourceSet) {

						Collection<ExternalDependency> dependencies = sourceSet.getDependencies();
						DomainObjectSet<? extends IdeaDependency> gradleDependencies = gradleModule.getDependencies();

						for (IdeaDependency dependency : gradleDependencies) {
							if (dependency instanceof InternalIdeaSingleEntryLibraryDependency) {
								InternalIdeaSingleEntryLibraryDependency localDependency =
									(InternalIdeaSingleEntryLibraryDependency)dependency;
								DefaultExternalLibraryDependency libraryDependency =
									new DefaultExternalLibraryDependency();

								File jarFile = localDependency.getFile();

								String jarName = jarFile.getName();

								if (jarName.endsWith(".jar")) {
									GradleModuleVersion gradleModuleVersion = localDependency.getGradleModuleVersion();

                                    assert gradleModuleVersion != null;

									libraryDependency.setName(gradleModuleVersion.getName());

									libraryDependency.setGroup(gradleModuleVersion.getGroup());

									libraryDependency.setVersion(gradleModuleVersion.getVersion());

									libraryDependency.setFile(jarFile);

									libraryDependency.setSource(localDependency.getSource());

									InternalIdeaDependencyScope jarScope = localDependency.getScope();

									libraryDependency.setScope(jarScope.getScope());

									libraryDependency.setExported(localDependency.getExported());
								}

								dependencies.add(libraryDependency);
							}
						}

						GradleProjectResolverUtil.buildDependencies(
							resolverCtx, sourceSetMap, artifactsMap, dataNode, dependencies, ideProject);
					}

				});
		}
	}

	@Nullable
	private static ExternalProject _getExternalProject(
		@NotNull IdeaModule gradleModule, @NotNull ProjectResolverContext resolverCtx) {

		ExternalProject project = resolverCtx.getExtraProject(gradleModule, ExternalProject.class);

		if ((project == null) && resolverCtx.isResolveModulePerSourceSet()) {
			_LOG.error("External Project model is missing for module-per-sourceSet import mode.");
		}

		return project;
	}

	private static void _processSourceSets(
		@NotNull ProjectResolverContext resolverCtx, @NotNull IdeaModule gradleModule,
		@NotNull ExternalProject externalProject, @NotNull DataNode<ModuleData> ideModule,
		@NotNull SourceSetsProcessor processor) {

		Map<String, DataNode<GradleSourceSetData>> sourceSetsMap = new HashMap<>();

		for (DataNode<GradleSourceSetData> dataNode :
				ExternalSystemApiUtil.findAll(ideModule, GradleSourceSetData.KEY)) {

			GradleSourceSetData gradleSourceSetData = dataNode.getData();

			sourceSetsMap.put(gradleSourceSetData.getId(), dataNode);
		}

		Map<String, ? extends ExternalSourceSet> externalSourceSetsMap = externalProject.getSourceSets();

		for (ExternalSourceSet sourceSet : externalSourceSetsMap.values()) {
			Map<? extends IExternalSystemSourceType, ? extends ExternalSourceDirectorySet> sources =
				sourceSet.getSources();

			if (sources.isEmpty())

				continue;

			final String moduleId = GradleProjectResolverUtil.getModuleId(resolverCtx, gradleModule, sourceSet);

			final DataNode<? extends ModuleData> moduleDataNode =
				sourceSetsMap.isEmpty() ? ideModule : sourceSetsMap.get(moduleId);

			if (moduleDataNode == null)

				continue;

			processor.process(gradleModule, moduleDataNode, sourceSet);
		}
	}

	private static final Logger _LOG = Logger.getInstance(LiferayGradleProjectResolverExtension.class);

	private interface SourceSetsProcessor {

		public void process(
			@NotNull IdeaModule gradleModule, @NotNull DataNode<? extends ModuleData> dataNode,
			@NotNull ExternalSourceSet sourceSet);

	}

}