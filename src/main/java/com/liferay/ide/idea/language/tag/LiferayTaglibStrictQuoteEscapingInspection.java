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

package com.liferay.ide.idea.language.tag;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.jsp.impl.CustomTagDescriptorBase;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JspPsiUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.impl.source.jsp.jspXml.JspExpression;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.xml.XmlElementDescriptor;

import com.liferay.ide.idea.core.LiferayInspectionsConstants;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibStrictQuoteEscapingInspection extends XmlSuppressableInspectionTool {

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean onTheFly) {
		return new XmlElementVisitor() {

			@Override
			public void visitXmlAttribute(XmlAttribute xmlAttribute) {
				XmlAttributeValue xmlAttributeValue = xmlAttribute.getValueElement();

				if (xmlAttributeValue == null) {
					return;
				}

				if (_isDoubleQuoted(xmlAttributeValue.getText())) {
					XmlTag xmlTag = PsiTreeUtil.getParentOfType(xmlAttribute, XmlTag.class);

					if (xmlTag == null) {
						return;
					}

					XmlElementDescriptor xmlElementDescriptor = xmlTag.getDescriptor();

					if (xmlElementDescriptor instanceof CustomTagDescriptorBase) {
						JspExpression[] jspExpressions = PsiTreeUtil.getChildrenOfType(
							xmlAttributeValue, JspExpression.class);

						if (jspExpressions == null) {
							return;
						}

						Stream.of(
							jspExpressions
						).map(
							jspExpression -> PsiTreeUtil.getChildrenOfType(jspExpression, XmlText.class)
						).filter(
							Objects::nonNull
						).flatMap(
							Arrays::stream
						).map(
							XmlText::getText
						).filter(
							LiferayTaglibStrictQuoteEscapingInspection::_containsUnescapedQuotes
						).forEach(
							text -> problemsHolder.registerProblem(
								xmlAttributeValue, _PROBLEM_DESCRIPTION, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
								new UseSingleQuotesFix())
						);
					}
				}
			}

		};
	}

	@Nls
	@NotNull
	@Override
	public String getDisplayName() {
		return "Strict quote escaping for taglib attributes";
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
		return new String[] {getGroupDisplayName(), LiferayInspectionsConstants.JSP_GROUP_NAME};
	}

	@Override
	public String getStaticDescription() {
		return "Check for unescaped double quotes inside double quoted taglib attributes.";
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	private static boolean _containsUnescapedQuotes(String text) {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			if ((c == '\"') && ((i == 0) || (text.charAt(i - 1) != '\\'))) {
				return true;
			}
		}

		return false;
	}

	private static boolean _isDoubleQuoted(String s) {
		if ((s.length() > 1) && (s.charAt(0) == '\"') && (s.charAt(0) == s.charAt(s.length() - 1))) {
			return true;
		}

		return false;
	}

	private static final String _PROBLEM_DESCRIPTION =
		"Attribute value is quoted with \" which must be escaped when used within the value";

	private static class UseSingleQuotesFix implements LocalQuickFix {

		@Override
		public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
			PsiElement psiElement = problemDescriptor.getPsiElement();

			JspFile jspFile = JspPsiUtil.getJspFile(psiElement);

			if (jspFile != null) {
				XmlAttributeValue xmlAttributeValue = (XmlAttributeValue)psiElement;

				TextRange textRange = psiElement.getTextRange();

				PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);

				Document document = psiDocumentManager.getDocument(jspFile);

				if (document != null) {
					psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);

					String oldText = xmlAttributeValue.getText();

					String newText = "\'" + StringUtil.unquoteString(oldText, '\"') + "\'";

					document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), newText);

					psiDocumentManager.commitDocument(document);
				}
			}
		}

		@Nls(capitalization = Nls.Capitalization.Sentence)
		@NotNull
		@Override
		public String getFamilyName() {
			return "Use single quotes";
		}

	}

}