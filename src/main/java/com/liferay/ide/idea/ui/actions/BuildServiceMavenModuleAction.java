/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ListUtil;

import java.util.Arrays;

/**
 * @author Ethan Sun
 */
public class BuildServiceMavenModuleAction extends AbstractLiferayMavenGoalAction implements LiferayWorkspaceSupport {

	public BuildServiceMavenModuleAction() {
		super("BuildService", "Run buildService goal", LiferayIcons.LIFERAY_ICON);

		goals = Arrays.asList("service-builder:build");
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		if (super.isEnabledAndVisible(anActionEvent)) {
			return ListUtil.isNotEmpty(getServiceBuilderModules(anActionEvent));
		}

		return false;
	}

}