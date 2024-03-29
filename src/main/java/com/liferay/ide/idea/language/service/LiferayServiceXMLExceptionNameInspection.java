/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.service;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlText;

import com.liferay.ide.idea.core.LiferayInspectionsConstants;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLExceptionNameInspection extends XmlSuppressableInspectionTool {

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean onTheFly) {
		return new XmlElementVisitor() {

			@Override
			public void visitXmlText(XmlText xmlText) {
				if (LiferayServiceXMLUtil.isExceptionTag(xmlText)) {
					String text = xmlText.getText();

					if ((text != null) && text.endsWith("Exception")) {
						problemsHolder.registerProblem(
							xmlText, "Do not add Exception at the end of the name",
							ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new RemoveExceptionSuffixFix());
					}
				}
			}

		};
	}

	@Nls
	@NotNull
	@Override
	public String getDisplayName() {
		return "unneccessary service.xml exception suffix";
	}

	@Nls
	@NotNull
	@Override
	public String getGroupDisplayName() {
		return LiferayInspectionsConstants.LIFERAY_GROUP_NAME;
	}

	@NotNull
	@Override
	public String[] getGroupPath() {
		return new String[] {getGroupDisplayName(), LiferayInspectionsConstants.SERVICE_XML_GROUP_NAME};
	}

	@Nullable
	@Override
	public String getStaticDescription() {
		return "Check for unneccessary Exception suffix at service.xml exception entries.";
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	private static class RemoveExceptionSuffixFix implements LocalQuickFix {

		@Override
		public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
			PsiElement psiElement = descriptor.getPsiElement();

			PsiFile containingPsiFile = psiElement.getContainingFile();

			XmlText xmlText = (XmlText)psiElement;

			TextRange textRange = psiElement.getTextRange();

			PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);

			Document document = psiDocumentManager.getDocument(containingPsiFile);

			psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);

			String oldText = xmlText.getText();

			String newText = oldText.substring(0, oldText.length() - "Exception".length());

			document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), newText);

			psiDocumentManager.commitDocument(document);
		}

		@Nls(capitalization = Nls.Capitalization.Sentence)
		@NotNull
		@Override
		public String getFamilyName() {
			return "Remove Exception suffix";
		}

	}

}