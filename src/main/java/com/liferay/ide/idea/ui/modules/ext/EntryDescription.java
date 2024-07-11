/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules.ext;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.Icon;

/**
 * @author Charles Wu
 */
public class EntryDescription {

	public EntryDescription(VirtualFile virtualFile) {
		_virtualFile = virtualFile;

		String fileUrl = _virtualFile.getUrl();

		try {
			_presentableUrl = fileUrl.split("jar!/")[1];

			if (_virtualFile.isDirectory()) {
				_presentableUrl += "/";
			}
		}
		catch (Exception exception) {
			_presentableUrl = fileUrl;
		}
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof EntryDescription)) {
			return false;
		}

		EntryDescription entryDescription = (EntryDescription)obj;

		return Comparing.equal(entryDescription.getVirtualFile(), _virtualFile);
	}

	public Icon getIconFor() {
		if (_virtualFile.isDirectory()) {
			return AllIcons.Nodes.Folder;
		}

		Icon icon = AllIcons.FileTypes.Unknown;

		String fileExtension = _virtualFile.getExtension();

		if (fileExtension != null) {
			switch (fileExtension) {
				case "css":
					icon = AllIcons.FileTypes.Css;

					break;
				case "html":
					icon = AllIcons.FileTypes.Html;

					break;
				case "java":
					icon = AllIcons.FileTypes.Java;

					break;
				case "jsp":
					icon = AllIcons.FileTypes.Jsp;

					break;
				case "properties":
					icon = AllIcons.FileTypes.Properties;

					break;
				case "xml":
					icon = AllIcons.FileTypes.Xml;

					break;
			}
		}

		return icon;
	}

	public String getPresentableUrl() {
		return _presentableUrl;
	}

	public VirtualFile getVirtualFile() {
		return _virtualFile;
	}

	@Override
	public int hashCode() {
		String string = _virtualFile.toString();

		return string.hashCode();
	}

	public boolean isValid() {
		return _virtualFile.isValid();
	}

	private String _presentableUrl;
	private VirtualFile _virtualFile;

}