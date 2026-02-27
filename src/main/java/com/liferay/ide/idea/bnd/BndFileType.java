/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayIcons;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 * @author Joye Luo
 */
public class BndFileType extends LanguageFileType {

	public static final LanguageFileType INSTANCE = new BndFileType();

	public BndFileType() {
		super(BndLanguage.INSTANCE);
	}

	@Nullable
	@Override
	public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
		return null;
	}

	@NotNull
	@Override
	public String getDefaultExtension() {
		return "bnd";
	}

	@NotNull
	@Override
	public String getDescription() {
		return "bnd file (Liferay)";
	}

	@Nullable
	@Override
	public Icon getIcon() {
		return LiferayIcons.BND_ICON;
	}

	@NotNull
	@Override
	public String getName() {
		return "bnd file (Liferay)";
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

}