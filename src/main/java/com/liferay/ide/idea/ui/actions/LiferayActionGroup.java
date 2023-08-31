/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.actions;

import com.intellij.ide.actions.NonTrivialActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbService;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Andy Wu
 */
public class LiferayActionGroup extends NonTrivialActionGroup {

	@Override
	public boolean isDumbAware() {
		return false;
	}

	public void update(AnActionEvent event) {
		Presentation presentation = event.getPresentation();

		DumbService dumbService = DumbService.getInstance(event.getProject());

		if (dumbService.isDumb()) {
			presentation.setEnabled(false);

			return;
		}

		AnAction[] actions = getChildren(event);

		Supplier<Stream<AnAction>> streamSupplier = () -> Stream.of(actions);

		Stream<AnAction> stream = streamSupplier.get();

		long count = stream.filter(
			action -> action instanceof AbstractLiferayGradleTaskAction
		).map(
			action -> (AbstractLiferayGradleTaskAction)action
		).filter(
			action -> action.isEnabledAndVisible(event)
		).count();

		if (count <= 0) {
			stream = streamSupplier.get();

			count = stream.filter(
				action -> action instanceof AbstractLiferayMavenGoalAction
			).map(
				action -> (AbstractLiferayMavenGoalAction)action
			).filter(
				action -> action.isEnabledAndVisible(event)
			).count();
		}

		presentation.setEnabledAndVisible(count > 0);
	}

}