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

package com.liferay.ide.idea.bnd.psi;

import com.intellij.psi.PsiFile;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public interface BndFile extends PsiFile {

	/**
	 * Returns the header from the main section with the given name, or null if no such header exists.
	 */
	@Nullable
	public BndHeader getHeader(@NotNull String name);

	/**
	 * Returns all headers from the main section in this file.
	 */
	@NotNull
	public List<BndHeader> getHeaders();

	/**
	 * Returns main (first) section if not empty.
	 */
	@Nullable
	public BndSection getMainSection();

	/**
	 * Returns all sections of the file.
	 */
	@NotNull
	public List<BndSection> getSections();

}