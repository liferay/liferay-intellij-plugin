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

package com.liferay.ide.idea.language.jsp;

import com.intellij.debugger.engine.SourcesFinder;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.javaee.deployment.JspDeploymentManager;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.externalSystem.model.project.LibraryPathType;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ContainerUtil;

import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.IntellijUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayJspDebuggerSourceFinderAdapter implements SourcesFinder<JavaeeFacet[]> {

	@Nullable
	public PsiFile findSourceFile(String relPath, Project project, JavaeeFacet[] scope) {
		List<PsiFile> results = findSourceFiles(relPath, project, scope);

		if (!results.isEmpty()) {
			return results.get(0);
		}

		return null;
	}

	@NotNull
	public List<PsiFile> findSourceFiles(String relPath, Project project, JavaeeFacet[] scope) {
		if (_isJava(relPath)) {
			return Collections.emptyList();
		}

		List<PsiFile> sourceFiles = new ArrayList<>();

		JspDeploymentManager jspDeploymentManager = JspDeploymentManager.getInstance();

		PsiFile deployedJspSourceFromFacets = jspDeploymentManager.getDeployedJspSource(relPath, project, scope);

		if (deployedJspSourceFromFacets != null) {
			sourceFiles.add(deployedJspSourceFromFacets);
		}

		ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

		OrderEnumerator orderEntries = projectRootManager.orderEntries();

		orderEntries.forEachLibrary(
			library -> {
				VirtualFile[] virtualFiles = library.getFiles(OrderRootType.CLASSES);

				for (VirtualFile virtualFile : virtualFiles) {
					_addJspFiles(relPath, project, sourceFiles, IntellijUtil.getJarRoot(virtualFile));
				}

				return true;
			});

		List<LibraryData> targetPlatformArtifacts = _getTargetPlatformArtifacts(project);

		targetPlatformArtifacts.stream(
		).map(
			libraryData -> {
				Set<String> sourcePaths = libraryData.getPaths(LibraryPathType.SOURCE);

				return ContainerUtil.getFirstItem(sourcePaths);
			}
		).filter(
			Objects::nonNull
		).map(
			sourcePath -> {
				LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

				return localFileSystem.findFileByPath(sourcePath);
			}
		).filter(
			Objects::nonNull
		).forEach(
			rootVirtualFile -> _addJspFiles(relPath, project, sourceFiles, IntellijUtil.getJarRoot(rootVirtualFile))
		);

		return sourceFiles;
	}

	@NotNull
	private static List<LibraryData> _getTargetPlatformArtifacts(@NotNull Project project) {
		Application application = ApplicationManager.getApplication();

		if (application.isUnitTestMode()) {
			return _targetPlatformArtifacts;
		}

		return GradleUtil.getTargetPlatformArtifacts(project);
	}

	private void _addJspFiles(String relPath, Project project, Collection<PsiFile> psiFiles, VirtualFile jarRoot) {
		PsiManager psiManager = PsiManager.getInstance(project);

		Optional.ofNullable(
			jarRoot
		).map(
			virtualFile -> IntellijUtil.getChild(virtualFile, "META-INF/resources")
		).filter(
			Objects::nonNull
		).map(
			child -> IntellijUtil.getChild(child, relPath)
		).filter(
			Objects::nonNull
		).map(
			psiManager::findFile
		).filter(
			Objects::nonNull
		).ifPresent(
			psiFiles::add
		);
	}

	private boolean _isJava(String relPath) {
		FileTypeManager fileTypeManager = FileTypeManager.getInstance();

		List<FileNameMatcher> fileNameMatchers = fileTypeManager.getAssociations(JavaFileType.INSTANCE);

		Optional<FileNameMatcher> fileNameMatcher = fileNameMatchers.stream(
		).filter(
			matcher -> matcher.acceptsCharSequence(relPath)
		).findFirst();

		return fileNameMatcher.isPresent();
	}

	private static List<LibraryData> _targetPlatformArtifacts = new ArrayList<>();

}