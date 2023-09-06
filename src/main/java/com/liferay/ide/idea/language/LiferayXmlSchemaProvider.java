/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.XmlSchemaProvider;

import java.net.URL;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides XML Schema files for portlet-model-hints.xml and custom-sql/default.xml
 *
 * @author Dominik Marks
 */
public class LiferayXmlSchemaProvider extends XmlSchemaProvider {

	@Nullable
	@Override
	public XmlFile getSchema(@NotNull String url, @Nullable Module module, @NotNull PsiFile baseFile) {
		URL schemaFileURL = null;

		PsiFile psiFile = baseFile;

		if (baseFile.getOriginalFile() != null) {
			psiFile = baseFile.getOriginalFile();
		}

		String fileName = psiFile.getName();

		if (Objects.equals(fileName, "portal-model-hints.xml") || Objects.equals(fileName, "ext-model-hints.xml") ||
			Objects.equals(fileName, "portlet-model-hints.xml") ||
			Objects.equals(fileName, "portlet-model-hints-ext.xml")) {

			schemaFileURL = LiferayXmlSchemaProvider.class.getResource(
				"/definitions/xsd/liferay-portlet-model-hints_7_0_0.xsd");
		}
		else if (Objects.equals(fileName, "default.xml")) {
			PsiDirectory psiDirectory = psiFile.getParent();

			if (psiDirectory != null) {
				String psiDirectoryName = psiDirectory.getName();

				if (Objects.equals(psiDirectoryName, "custom-sql")) {
					schemaFileURL = LiferayXmlSchemaProvider.class.getResource(
						"/definitions/xsd/liferay-custom-sql_7_0_0.xsd");
				}
			}
		}

		if (schemaFileURL != null) {
			VirtualFile virtualFile = VfsUtil.findFileByURL(schemaFileURL);

			if (virtualFile != null) {
				PsiManager psiManager = PsiManager.getInstance(baseFile.getProject());

				PsiFile targetFile = psiManager.findFile(virtualFile);

				if (targetFile instanceof XmlFile) {
					return (XmlFile)targetFile;
				}
			}
		}

		return null;
	}

	@Override
	public boolean isAvailable(@NotNull XmlFile xmlFile) {
		PsiFile psiFile = xmlFile;

		if (xmlFile.getOriginalFile() != null) {
			psiFile = xmlFile.getOriginalFile();
		}

		if (psiFile.getFileType() != XmlFileType.INSTANCE) {
			return false;
		}

		String fileName = psiFile.getName();

		if (Objects.equals(fileName, "portal-model-hints.xml") || Objects.equals(fileName, "ext-model-hints.xml") ||
			Objects.equals(fileName, "portlet-model-hints.xml") ||
			Objects.equals(fileName, "portlet-model-hints-ext.xml")) {

			return true;
		}

		if (Objects.equals(fileName, "default.xml")) {
			PsiDirectory psiDirectory = psiFile.getParent();

			if (psiDirectory != null) {
				String psiDirectoryName = psiDirectory.getName();

				if (Objects.equals(psiDirectoryName, "custom-sql")) {
					return true;
				}
			}
		}

		return false;
	}

}