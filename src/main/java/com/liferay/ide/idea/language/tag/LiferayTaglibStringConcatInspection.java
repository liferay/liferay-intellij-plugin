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
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
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
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.xml.XmlElementDescriptor;

import com.liferay.ide.idea.core.LiferayInspectionsConstants;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibStringConcatInspection extends XmlSuppressableInspectionTool {

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean onTheFly) {
		return new XmlElementVisitor() {

			@Override
			public void visitXmlAttribute(XmlAttribute xmlAttribute) {
				if (xmlAttribute.getValueElement() == null) {
					return;
				}

				XmlTag xmlTag = PsiTreeUtil.getParentOfType(xmlAttribute, XmlTag.class);

				if (xmlTag == null) {
					return;
				}

				XmlElementDescriptor xmlElementDescriptor = xmlTag.getDescriptor();

				if ((xmlElementDescriptor != null) &&
					_isRuntimeExpressionAttribute(xmlElementDescriptor, xmlAttribute.getName()) &&
					_containsTextAndJspExpressions(xmlAttribute.getValueElement())) {

					problemsHolder.registerProblem(
						xmlAttribute.getValueElement(),
						"JSP expessions and string values cannot be concatenated inside the attribute",
						ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new WrapInJSpExpression());
				}
			}

		};
	}

	@Nls
	@NotNull
	@Override
	public String getDisplayName() {
		return "String concatenation inside taglib attributes";
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
		return "Check for string concatenation together with jsp expessions.";
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	private boolean _containsTextAndJspExpressions(XmlAttributeValue xmlAttributeValue) {
		boolean hasValueToken = false;

		XmlToken[] xmlTokens = PsiTreeUtil.getChildrenOfType(xmlAttributeValue, XmlToken.class);

		if (xmlTokens != null) {
			hasValueToken = Stream.of(
				xmlTokens
			).anyMatch(
				xmlToken -> XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN.equals(xmlToken.getTokenType())
			);
		}

		JspExpression jspExpression = PsiTreeUtil.getChildOfType(xmlAttributeValue, JspExpression.class);

		if (hasValueToken && (jspExpression != null)) {
			return true;
		}

		return false;
	}

	private boolean _isRuntimeExpressionAttribute(XmlElementDescriptor xmlElementDescriptor, String name) {
		PsiElement declarationPsiElement = xmlElementDescriptor.getDeclaration();

		if (declarationPsiElement instanceof XmlTag) {
			XmlTag declarationXmlTag = (XmlTag)declarationPsiElement;

			XmlTag[] attributeXmlTags = declarationXmlTag.findSubTags("attribute");

			for (XmlTag attributeXmlTag : attributeXmlTags) {
				String attributeName = attributeXmlTag.getSubTagText("name");

				if (name.equals(attributeName)) {
					String rtexprvalue = attributeXmlTag.getSubTagText("rtexprvalue");

					return Objects.equals("true", rtexprvalue);
				}
			}
		}

		return false;
	}

	private static class WrapInJSpExpression implements LocalQuickFix {

		@Override
		public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
			PsiElement psiElement = problemDescriptor.getPsiElement();

			JspFile jspFile = JspPsiUtil.getJspFile(psiElement);

			if (jspFile == null) {
				return;
			}

			PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);

			Document document = psiDocumentManager.getDocument(jspFile);

			if (document == null) {
				return;
			}

			psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);

			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append("'<%=");

			boolean firstChild = true;

			XmlAttributeValue xmlAttributeValue = (XmlAttributeValue)psiElement;

			for (PsiElement childPsiElement : xmlAttributeValue.getChildren()) {
				if (childPsiElement instanceof XmlToken) {
					XmlToken xmlToken = (XmlToken)childPsiElement;

					if (XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN.equals(xmlToken.getTokenType())) {
						String text = xmlToken.getText();

						if (!firstChild) {
							stringBuilder.append(" + ");
						}

						stringBuilder.append(
							"\""
						).append(
							text
						).append(
							"\""
						);

						firstChild = false;
					}
				}
				else if (childPsiElement instanceof JspExpression) {
					JspExpression jspExpression = (JspExpression)childPsiElement;

					XmlText xmlText = PsiTreeUtil.getChildOfType(jspExpression, XmlText.class);

					if (xmlText != null) {
						if (!firstChild) {
							stringBuilder.append(" + ");
						}

						stringBuilder.append(
							"("
						).append(
							xmlText.getText()
						).append(
							")"
						);

						firstChild = false;
					}
				}
			}

			stringBuilder.append("%>'");

			TextRange textRange = psiElement.getTextRange();

			document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), stringBuilder.toString());

			psiDocumentManager.commitDocument(document);
		}

		@Nls(capitalization = Nls.Capitalization.Sentence)
		@NotNull
		@Override
		public String getFamilyName() {
			return "Wrap in JSP expression";
		}

	}

}