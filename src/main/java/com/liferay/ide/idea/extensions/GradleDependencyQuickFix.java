/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.extensions;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.core.MessagesBundle;
import com.liferay.ide.idea.util.GradleUtil;
import com.liferay.ide.idea.util.IntellijUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Charles Wu
 */
public class GradleDependencyQuickFix implements HighPriorityAction, Iconable, IntentionAction, LocalQuickFix {

	public GradleDependencyQuickFix(Module module, Library library) {
		_module = module;
		_library = library;
	}

	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
		PsiElement psiElement = problemDescriptor.getPsiElement();

		invoke(project, null, psiElement.getContainingFile());
	}

	@NotNull
	@Override
	public String getFamilyName() {
		return MessagesBundle.message("orderEntry.fix.family.add.library.to.gradle");
	}

	public Icon getIcon(@IconFlags int flags) {
		return LiferayIcons.LIFERAY_ICON;
	}

	@NotNull
	@Override
	public String getText() {
		return MessagesBundle.message("orderEntry.fix.add.library.to.gradle", _library.getPresentableName());
	}

	@Override
	public void invoke(@NotNull Project project, @Nullable Editor editor, PsiFile psiFile) {
		String moduleName = _module.getName();
		PsiFile gradleFile = null;

		// presume it's a gradle source set module

		if (moduleName.contains("main")) {
			Module parentModule = IntellijUtil.getParentModule(_module);

			if (parentModule != null) {
				gradleFile = IntellijUtil.getModulePsiFileByName(parentModule, "build.gradle");
			}
		}

		if (gradleFile == null) {
			gradleFile = IntellijUtil.getModulePsiFileByName(_module, "build.gradle");
		}

		if (gradleFile == null) {
			return;
		}

		String libraryName = _library.getName();

		String result = libraryName.split(GradleDependencyQuickFixProvider.GRADLE_LIBRARY_PREFIX)[1];

		try {
			if (LiferayWorkspaceSupport.getTargetPlatformVersion(project) != null) {
				result = result.substring(0, result.lastIndexOf(":"));
			}
		}
		catch (Exception exception) {
		}

		GradleUtil.addGradleDependencies(gradleFile, result);
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
		LibraryEx libraryEx = (LibraryEx)_library;

		if (!project.isDisposed() && !_module.isDisposed() && !libraryEx.isDisposed()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean startInWriteAction() {
		return false;
	}

	private final Library _library;
	private final Module _module;

}