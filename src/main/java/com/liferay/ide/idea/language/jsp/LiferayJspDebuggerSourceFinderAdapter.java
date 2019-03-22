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
import com.intellij.javaee.deployment.JspDeploymentManager;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.externalSystem.model.project.LibraryPathType;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ContainerUtil;

import com.liferay.ide.idea.util.IntellijUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayJspDebuggerSourceFinderAdapter implements SourcesFinder<JavaeeFacet[]> {

    @Nullable
    public PsiFile findSourceFile(String relPath, Project project, JavaeeFacet[] scope) {
        Collection<PsiFile> results = findSourceFiles(relPath, project, scope);

        if (!results.isEmpty()) {
            Iterator<PsiFile> iterator = results.iterator();

            return iterator.next();
        }

        return null;
    }

    @NotNull
    public Collection<PsiFile> findSourceFiles(String relPath, Project project, JavaeeFacet[] scope) {
        Collection<PsiFile> sourceFiles = new ArrayList<>();

        if (_isJava(relPath)) {
            return sourceFiles;
        }

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
                    VirtualFile jarRoot = IntellijUtil.getJarRoot(virtualFile);

                    _addJspFiles(relPath, project, sourceFiles, jarRoot);
                }
                return true;
            }
        );

        List<LibraryData> targetPlatformArtifacts = _getTargetPlatformArtifacts(project);

        Stream<LibraryData> libraryDataStream = targetPlatformArtifacts.stream();

        libraryDataStream.forEach(
            libraryData -> {
                Set<String> sourcePaths = libraryData.getPaths(LibraryPathType.SOURCE);

                String sourcePath = ContainerUtil.getFirstItem(sourcePaths);

                if (sourcePath != null) {
                    LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

                    VirtualFile rootVirtualFile = localFileSystem.findFileByPath(sourcePath);

                    if (rootVirtualFile != null) {
                        VirtualFile jarRoot = IntellijUtil.getJarRoot(rootVirtualFile);

                        _addJspFiles(relPath, project, sourceFiles, jarRoot);
                    }
                }
            }
        );

        return sourceFiles;
    }

    @NotNull
    private static List<LibraryData> _getTargetPlatformArtifacts(@NotNull Project project) {
        Application application = ApplicationManager.getApplication();

        if (application.isUnitTestMode()) {
            return _targetPlatformArtifacts;
        }

        return LiferayWorkspaceUtil.getTargetPlatformArtifacts(project);
    }

    private void _addJspFiles(String relPath, Project project, Collection<PsiFile> psiFiles, VirtualFile jarRoot) {
        if (jarRoot != null) {
            VirtualFile child = IntellijUtil.getChild(jarRoot, "META-INF/resources");

            if (child != null) {
                VirtualFile virtualFile = IntellijUtil.getChild(child, relPath);

                if (virtualFile != null) {
                    PsiManager psiManager = PsiManager.getInstance(project);

                    PsiFile psiFile = psiManager.findFile(virtualFile);

                    if (psiFile != null) {
                        psiFiles.add(psiFile);
                    }
                }
            }
        }
    }

    private boolean _isJava(String relPath) {
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();

        List<FileNameMatcher> fileNameMatchers = fileTypeManager.getAssociations(StdFileTypes.JAVA);

        Iterator iterator = fileNameMatchers.iterator();

        FileNameMatcher matcher;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            matcher = (FileNameMatcher)iterator.next();
        } while (!matcher.accept(relPath));

        return true;
    }

    private static List<LibraryData> _targetPlatformArtifacts = new ArrayList<>();

}
