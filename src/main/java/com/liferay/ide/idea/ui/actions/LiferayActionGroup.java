/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.actions;

import com.intellij.ide.actions.NonTrivialActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Andy Wu
 */
public class LiferayActionGroup extends NonTrivialActionGroup {

	@Override
	public boolean isDumbAware() {
		return false;
	}

	@NotNull
	@Override
	@Unmodifiable
	public List<? extends AnAction> postProcessVisibleChildren(
		AnActionEvent event, List<? extends AnAction> visibleChildren) {

		Presentation presentation = event.getPresentation();

		Stream<? extends AnAction> stream = visibleChildren.stream();

		presentation.setEnabled(
			stream.filter(
				action -> action instanceof AbstractLiferayAction
			).map(
				action -> (AbstractLiferayAction)action
			).anyMatch(
				action -> action.isEnabledAndVisible(event)
			));

		return super.postProcessVisibleChildren(event, visibleChildren);
	}

}