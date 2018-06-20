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

package com.liferay.ide.idea.ui.actions;

import icons.LiferayIcons;

import java.util.Arrays;

/**
 * @author Joye Luo
 */
public class InitBundleMavenAction extends AbstractLiferayMavenGoalAction {

	public InitBundleMavenAction() {
		super("InitBundle", "Run initBundle goal", LiferayIcons.LIFERAY_ICON);

		goals = Arrays.asList("bundle-support:init");
	}

}