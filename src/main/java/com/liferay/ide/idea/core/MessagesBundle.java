/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.core;

import com.intellij.AbstractBundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * @author Charles Wu
 */
public class MessagesBundle extends AbstractBundle {

	public static final String MESSAGES = "messages.Messages";

	public static String message(
		@NotNull @PropertyKey(resourceBundle = MESSAGES) String key, @NotNull Object... params) {

		return _bundle.getMessage(key, params);
	}

	public MessagesBundle() {
		super(MESSAGES);
	}

	private static final MessagesBundle _bundle = new MessagesBundle();

}