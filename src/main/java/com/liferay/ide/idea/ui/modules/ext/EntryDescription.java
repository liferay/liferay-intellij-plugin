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

	public boolean isValid() {
		return _virtualFile.isValid();
	}

	private String _presentableUrl;
	private VirtualFile _virtualFile;

}