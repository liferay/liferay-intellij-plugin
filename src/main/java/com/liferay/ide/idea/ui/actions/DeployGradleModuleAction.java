/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;

import com.liferay.ide.idea.core.LiferayIcons;

/**
 * @author Andy Wu
 * @author Simon Jiang
 */
public class DeployGradleModuleAction extends AbstractLiferayGradleTaskAction {

	public DeployGradleModuleAction() {
		super("Deploy", "Run deploy task", LiferayIcons.LIFERAY_ICON, "deploy");
	}

	@Override
	protected boolean isEnabledAndVisible(AnActionEvent anActionEvent) {
		if (super.isEnabledAndVisible(anActionEvent)) {
			return verifyModuleDeploy(anActionEvent);
		}

		return false;
	}

}