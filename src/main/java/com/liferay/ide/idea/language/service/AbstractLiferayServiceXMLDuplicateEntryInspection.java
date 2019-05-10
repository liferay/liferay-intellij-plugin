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

import com.liferay.ide.idea.util.LiferayInspectionsConstants;

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
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean onTheFly) {
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
                                XmlTag parentTag = PsiTreeUtil.getParentOfType(xmlTag, XmlTag.class);

                                if (parentTag != null) {
                                    List<XmlTag> xmlTags = _getXmlTagsWithAttributeValue(parentTag, xmlTag.getLocalName(), xmlAttribute.getName(), text);

                                    if (xmlTags.size() > 1) {
                                        holder.registerProblem(xmlAttributeValue,
                                            "Duplicate entry",
                                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            new RemoveXmlTagFix()
                                        );
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
                                holder.registerProblem(xmlText,
                                    "Duplicate entry",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    new RemoveXmlTagFix()
                                );
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
        return new String[]{
            getGroupDisplayName(),
            LiferayInspectionsConstants.SERVICE_XML_GROUP_NAME
        };
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    protected abstract boolean isSuitableXmlAttributeValue(XmlAttributeValue xmlAttributeValue);

    protected abstract boolean isSuitableXmlText(XmlText xmlText);

    private List<XmlTag> _getXmlTagsWithAttributeValue(XmlTag parentTag, String localName, String attributeName, String attributeValue) {
        List<XmlTag> result = new ArrayList<>();

        for (XmlTag xmlTag : parentTag.getSubTags()) {
            if (localName.equals(xmlTag.getLocalName())) {
                XmlAttribute attribute = xmlTag.getAttribute(attributeName);

                if (attribute != null) {
                    if (attributeValue.equals(attribute.getValue())) {
                        result.add(xmlTag);
                    }
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
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();

            PsiFile containingFile = element.getContainingFile();

            XmlTag xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag.class);

            if (xmlTag != null) {
                XmlText spacerText = PsiTreeUtil.getPrevSiblingOfType(xmlTag, XmlText.class);
                XmlTag parentTag = PsiTreeUtil.getParentOfType(xmlTag, XmlTag.class);

                if (parentTag != null) {

                    WriteCommandAction.Builder writeCommandActionBuilder = WriteCommandAction.writeCommandAction(project, containingFile);

                    writeCommandActionBuilder.run(() -> {
                        parentTag.getNode().removeChild(xmlTag.getNode());

                        if (spacerText != null) {
                            parentTag.getNode().removeChild(spacerText.getNode());
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

