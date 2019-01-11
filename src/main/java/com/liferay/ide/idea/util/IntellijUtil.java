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
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Charles Wu
 */
public class IntellijUtil {

	@Nullable
	public static PsiFile getModulePsiFileByName(@NotNull Module module, String fileName) {
		PsiFile[] files = getModulePsiFilesByName(module, fileName);

		if (files.length > 0) {
			assert files.length < 2;

			return files[0];
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
		ModuleManager instance = ModuleManager.getInstance(module.getProject());

		ModifiableModuleModel modifiableModel = instance.getModifiableModel();

		String[] groupPaths = modifiableModel.getModuleGroupPath(module);

		String path = ArrayUtil.getLastElement(groupPaths);

		if (path != null) {
			for (Module m : modifiableModel.getModules()) {
				if (path.equals(m.getName())) {
					return m;
				}
			}
		}

		return null;
	}

	public static PsiFile[] getProjectPsiFilesByName(Project project, String name) {
		return FilenameIndex.getFilesByName(project, name, GlobalSearchScope.projectScope(project));
	}

}