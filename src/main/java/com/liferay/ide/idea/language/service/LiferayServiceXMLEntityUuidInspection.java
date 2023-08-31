/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.service;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;

import com.liferay.ide.idea.core.LiferayInspectionsConstants;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLEntityUuidInspection extends XmlSuppressableInspectionTool {

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean onTheFloy) {
		return new XmlElementVisitor() {

			@Override
			public void visitXmlAttributeValue(XmlAttributeValue value) {
				if (LiferayServiceXMLUtil.isEntityUuidAttribute(value)) {
					String text = value.getValue();

					if (Objects.equals(text, "true")) {
						XmlTag xmlTag = PsiTreeUtil.getParentOfType(value, XmlTag.class);

						if (xmlTag != null) {
							XmlTag[] childXmlTags = PsiTreeUtil.getChildrenOfType(xmlTag, XmlTag.class);

							boolean hasPrimaryColumn = false;

							if (childXmlTags != null) {
								hasPrimaryColumn = Stream.of(
									childXmlTags
								).filter(
									child -> Objects.equals(child.getName(), "column")
								).map(
									primary -> primary.getAttributeValue("primary")
								).anyMatch(
									"true"::equals
								);
							}

							if (!hasPrimaryColumn) {
								String entityName = xmlTag.getAttributeValue("name");

								problemsHolder.registerProblem(
									value,
									"Unable to create entity " + entityName + " with a UUID without a primary key",
									ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
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
	public String getDisplayName() {
		return "check that entities with a uuid have a primary key column";
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
		return "Unable to create entity with a UUID without a primary key";
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

}