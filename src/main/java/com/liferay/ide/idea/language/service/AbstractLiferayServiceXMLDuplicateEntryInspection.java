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

package com.liferay.ide.idea.language.service;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.psi.xml.XmlText;

import com.liferay.ide.idea.core.LiferayInspectionsConstants;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public abstract class AbstractLiferayServiceXMLDuplicateEntryInspection extends XmlSuppressableInspectionTool {

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean onTheFly) {
		return new XmlElementVisitor() {

			@Override
			public void visitXmlAttributeValue(XmlAttributeValue xmlAttributeValue) {
				if (isSuitableXmlAttributeValue(xmlAttributeValue)) {
					String text = xmlAttributeValue.getValue();

					if (StringUtil.isNotEmpty(text)) {
						XmlAttribute xmlAttribute = PsiTreeUtil.getParentOfType(xmlAttributeValue, XmlAttribute.class);

						if (xmlAttribute != null) {
							XmlTag xmlTag = PsiTreeUtil.getParentOfType(xmlAttribute, XmlTag.class);

							if (xmlTag != null) {
								XmlTag parentXmlTag = PsiTreeUtil.getParentOfType(xmlTag, XmlTag.class);

								if (parentXmlTag != null) {
									List<XmlTag> xmlTags = _getXmlTagsWithAttributeValue(
										parentXmlTag, xmlTag.getLocalName(), xmlAttribute.getName(), text);

									if (xmlTags.size() > 1) {
										problemsHolder.registerProblem(
											xmlAttributeValue, "Duplicate entry",
											ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new RemoveXmlTagFix());
									}
								}
							}
						}
					}
				}
			}

			@Override
			public void visitXmlText(XmlText xmlText) {
				if (isSuitableXmlText(xmlText)) {
					String text = xmlText.getText();

					XmlTag xmlTag = PsiTreeUtil.getParentOfType(xmlText, XmlTag.class);

					if (xmlTag != null) {
						XmlTag parentTag = PsiTreeUtil.getParentOfType(xmlTag, XmlTag.class);

						if (parentTag != null) {
							List<XmlTag> xmlTags = _getXmlTagsWithText(parentTag, xmlTag.getLocalName(), text);

							if (xmlTags.size() > 1) {
								problemsHolder.registerProblem(
									xmlText, "Duplicate entry", ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
									new RemoveXmlTagFix());
							}
						}
					}
				}
			}

		};
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

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	protected abstract boolean isSuitableXmlAttributeValue(XmlAttributeValue xmlAttributeValue);

	protected abstract boolean isSuitableXmlText(XmlText xmlText);

	private List<XmlTag> _getXmlTagsWithAttributeValue(
		XmlTag parentTag, String localName, String attributeName, String attributeValue) {

		List<XmlTag> result = new ArrayList<>();

		for (XmlTag xmlTag : parentTag.getSubTags()) {
			if (localName.equals(xmlTag.getLocalName())) {
				XmlAttribute attribute = xmlTag.getAttribute(attributeName);

				if ((attribute != null) && attributeValue.equals(attribute.getValue())) {
					result.add(xmlTag);
				}
			}
		}

		return result;
	}

	private List<XmlTag> _getXmlTagsWithText(XmlTag parentTag, String localName, String text) {
		List<XmlTag> result = new ArrayList<>();

		for (XmlTag xmlTag : parentTag.getSubTags()) {
			if (localName.equals(xmlTag.getLocalName())) {
				XmlTagValue xmlTagValue = xmlTag.getValue();

				if (text.equals(xmlTagValue.getText())) {
					result.add(xmlTag);
				}
			}
		}

		return result;
	}

	private static class RemoveXmlTagFix implements LocalQuickFix {

		@Override
		public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
			PsiElement psiElement = problemDescriptor.getPsiElement();

			PsiFile containingFile = psiElement.getContainingFile();

			XmlTag xmlTag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);

			if (xmlTag != null) {
				XmlTag parentXmlTag = PsiTreeUtil.getParentOfType(xmlTag, XmlTag.class);
				XmlText spacerXmlText = PsiTreeUtil.getPrevSiblingOfType(xmlTag, XmlText.class);

				if (parentXmlTag != null) {
					WriteCommandAction.Builder writeCommandActionBuilder = WriteCommandAction.writeCommandAction(
						project, containingFile);

					writeCommandActionBuilder.run(
						() -> {
							parentXmlTag.getNode(
							).removeChild(
								xmlTag.getNode()
							);

							if (spacerXmlText != null) {
								parentXmlTag.getNode(
								).removeChild(
									spacerXmlText.getNode()
								);
							}
						});
				}
			}
		}

		@Nls(capitalization = Nls.Capitalization.Sentence)
		@NotNull
		@Override
		public String getFamilyName() {
			return "Remove entry";
		}

	}

}