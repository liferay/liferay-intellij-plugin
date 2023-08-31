/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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