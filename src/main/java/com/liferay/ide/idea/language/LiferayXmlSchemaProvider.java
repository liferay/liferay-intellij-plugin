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
		URL schemaFileUrl = null;

		PsiFile psiFile = baseFile;

		if (baseFile.getOriginalFile() != null) {
			psiFile = baseFile.getOriginalFile();
		}

		String fileName = psiFile.getName();

		if ("portlet-model-hints.xml".equals(fileName)) {
			schemaFileUrl = LiferayXmlSchemaProvider.class.getResource(
				"/definitions/xsd/liferay-portlet-model-hints_7_0_0.xsd");
		}
		else if ("default.xml".equals(fileName)) {
			PsiDirectory psiDirectory = psiFile.getParent();

			if (psiDirectory != null) {
				String psiDirectoryName = psiDirectory.getName();

				if ("custom-sql".equals(psiDirectoryName)) {
					schemaFileUrl = LiferayXmlSchemaProvider.class.getResource(
						"/definitions/xsd/liferay-custom-sql_7_0_0.xsd");
				}
			}
		}

		if (schemaFileUrl != null) {
			VirtualFile virtualFile = VfsUtil.findFileByURL(schemaFileUrl);

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

		if ("portlet-model-hints.xml".equals(fileName)) {
			return true;
		}

		if ("default.xml".equals(fileName)) {
			PsiDirectory psiDirectory = psiFile.getParent();

			if (psiDirectory != null) {
				String psiDirectoryName = psiDirectory.getName();

				if ("custom-sql".equals(psiDirectoryName)) {
					return true;
				}
			}
		}

		return false;
	}

}