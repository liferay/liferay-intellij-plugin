package com.liferay.ide.idea.language;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
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
		URL targetFileUrl = null;

		PsiFile psiFile = baseFile;

		if (baseFile.getOriginalFile() != null) {
			psiFile = baseFile.getOriginalFile();
		}

		if (psiFile.getName().equals("portlet-model-hints.xml")) {
			targetFileUrl = LiferayXmlSchemaProvider.class.getResource(
			        "/definitions/liferay-portlet-model-hints_7_0_0.xsd"
            );
		} else if (
				(psiFile.getName().equals("default.xml")) &&
                (psiFile.getParent() != null) &&
                (psiFile.getParent().getName().equals("custom-sql"))
        ) {
			targetFileUrl = LiferayXmlSchemaProvider.class.getResource(
			        "/definitions/liferay-custom-sql_7_0_0.xsd"
            );
		}

		if (targetFileUrl != null) {
			VirtualFile virtualFile = VfsUtil.findFileByURL(targetFileUrl);

			if (virtualFile != null) {
				PsiFile targetFile = PsiManager.getInstance(baseFile.getProject()).findFile(virtualFile);

				if (targetFile instanceof XmlFile) {
					return (XmlFile)targetFile;
				}
			}
		}

		return null;
	}

	@Override
	public boolean isAvailable(@NotNull XmlFile file) {
		PsiFile psiFile = file;

		if (file.getOriginalFile() != null) {
			psiFile = file.getOriginalFile();
		}

		if (psiFile.getFileType() != XmlFileType.INSTANCE) {
			return false;
		}

		if (psiFile.getName().equals("portlet-model-hints.xml")) {
			return true;
		}

		if (psiFile.getName().equals("default.xml")) {
			if ((psiFile.getParent() != null) && (psiFile.getParent().getName().equals("custom-sql"))) {
				return true;
			}
		}

		return false;
	}

}