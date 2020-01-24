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

package com.liferay.ide.idea.bnd;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;

import icons.LiferayIcons;

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
		return "bnd file";
	}

	@Nullable
	@Override
	public Icon getIcon() {
		return LiferayIcons.BND_ICON;
	}

	@NotNull
	@Override
	public String getName() {
		return "bnd file";
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

}