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

package com.liferay.ide.idea.util;

import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Charles Wu
 */
public class IntellijUtil {

	public static VirtualFile getChild(VirtualFile parent, String name) {
		if (parent == null) {
			return null;
		}

		int index = name.indexOf('/');

		String pathElement = name;

		if (index > -1) {
			pathElement = name.substring(0, index);
		}

		for (VirtualFile virtualFile : parent.getChildren()) {
			if (pathElement.equals(virtualFile.getName())) {
				if (index == -1) {
					return virtualFile;
				}

				return getChild(virtualFile, name.substring(index + 1));
			}
		}

		return null;
	}

	public static VirtualFile getJarRoot(VirtualFile virtualFile) {
		VirtualFile jarRoot;

		VirtualFileSystem virtualFileSystem = virtualFile.getFileSystem();

		if (virtualFileSystem instanceof JarFileSystem) {
			JarFileSystem jarFileSystem = (JarFileSystem)virtualFileSystem;

			jarRoot = jarFileSystem.getRootByEntry(virtualFile);
		}
		else {
			JarFileSystem jarFileSystem = JarFileSystem.getInstance();

			jarRoot = jarFileSystem.getJarRootForLocalFile(virtualFile);
		}

		return jarRoot;
	}

	public static Module getModule(Project project, String moduleName) {
		Module module = null;

		final ProjectStructureConfigurable fromConfigurable = ProjectStructureConfigurable.getInstance(project);

		if (fromConfigurable != null) {
			ModuleStructureConfigurable modulesConfig = fromConfigurable.getModulesConfig();

			module = modulesConfig.getModule(moduleName);
		}
		else {
			ModuleManager moduleManager = ModuleManager.getInstance(project);

			module = moduleManager.findModuleByName(moduleName);
		}

		if (Objects.isNull(module)) {
			ModuleManager moduleManager = ModuleManager.getInstance(project);

			Module[] modules = moduleManager.getModules();

			for (Module moduleItem : modules) {
				String moduleItemName = moduleItem.getName();

				if (moduleItemName.endsWith(moduleName)) {
					return moduleItem;
				}
			}
		}

		return module;
	}

	@Nullable
	public static PsiFile getModulePsiFileByName(@NotNull Module module, String fileName) {
		PsiFile[] psiFiles = getModulePsiFilesByName(module, fileName);

		if (psiFiles.length > 0) {
			assert psiFiles.length < 2;

			return psiFiles[0];
		}

		return null;
	}

	public static PsiFile[] getModulePsiFilesByName(@NotNull Module module, String name) {
		return Stream.of(
			getProjectPsiFilesByName(module.getProject(), name)
		).filter(
			psiFile -> ModuleUtil.findModuleForFile(psiFile) == module
		).toArray(
			PsiFile[]::new
		);
	}

	@Nullable
	public static Module getParentModule(Module module) {
		ModuleManager moduleManager = ModuleManager.getInstance(module.getProject());

		ModifiableModuleModel modifiableModuleModel = moduleManager.getModifiableModel();

		String[] groupPaths = modifiableModuleModel.getModuleGroupPath(module);

		String path = ArrayUtil.getLastElement(groupPaths);

		if (path != null) {
			for (Module modifiableModule : modifiableModuleModel.getModules()) {
				if (path.equals(modifiableModule.getName())) {
					return modifiableModule;
				}
			}
		}

		return null;
	}

	public static PsiFile[] getProjectPsiFilesByName(Project project, String name) {
		return FilenameIndex.getFilesByName(project, name, GlobalSearchScope.projectScope(project));
	}

	public static boolean validateExistingModuleName(String moduleName) {
		if (Objects.isNull(moduleName)) {
			return false;
		}

		return moduleName.matches(_MODULE_NAME_REGEX);
	}

	private static final String _MODULE_NAME_REGEX = "([A-Za-z0-9_\\-.]+[A-Za-z0-9]$)|([A-Za-z0-9])";

}