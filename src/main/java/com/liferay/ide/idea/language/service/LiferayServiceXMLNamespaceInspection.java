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
import com.intellij.psi.xml.XmlText;

import com.liferay.ide.idea.core.LiferayInspectionsConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayServiceXMLNamespaceInspection extends XmlSuppressableInspectionTool {

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean onTheFly) {
		return new XmlElementVisitor() {

			@Override
			public void visitXmlText(XmlText xmlText) {
				if (LiferayServiceXMLUtil.isNamespaceTag(xmlText)) {
					String text = xmlText.getText();

					if (text != null) {
						Matcher matcher = _validNamespaceExpression.matcher(text);

						if (!matcher.matches()) {
							problemsHolder.registerProblem(
								xmlText, "Namespace is not valid", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
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
		return "check for valid namespace expression";
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
		return "Namespace must start with a letter or underscore followed by letters," +
			"numbers or underscores and may not be longer that 63 chars.";
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	private static final Pattern _validNamespaceExpression = Pattern.compile("[A-Za-z_]{1}[A-Za-z0-9_]{0,62}");

}