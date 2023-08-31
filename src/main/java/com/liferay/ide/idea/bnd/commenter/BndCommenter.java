/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.commenter;

import com.intellij.lang.Commenter;

import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class BndCommenter implements Commenter {

	@Nullable
	@Override
	public String getBlockCommentPrefix() {
		return null;
	}

	@Nullable
	@Override
	public String getBlockCommentSuffix() {
		return null;
	}

	@Nullable
	@Override
	public String getCommentedBlockCommentPrefix() {
		return null;
	}

	@Nullable
	@Override
	public String getCommentedBlockCommentSuffix() {
		return null;
	}

	@Nullable
	@Override
	public String getLineCommentPrefix() {
		return "#";
	}

}