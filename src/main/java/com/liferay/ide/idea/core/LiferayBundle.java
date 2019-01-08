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

package com.liferay.ide.idea.core;

import com.intellij.AbstractBundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * @author Charles Wu
 */
public class LiferayBundle extends AbstractBundle {

	public static final String PATH_TO_BUNDLE = "messages.LiferayBundle";

	public static String message(
		@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @NotNull Object... params) {

		return _bundle.getMessage(key, params);
	}

	public LiferayBundle() {
		super(PATH_TO_BUNDLE);
	}

	private static final LiferayBundle _bundle = new LiferayBundle();

}