/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.jsp;

import com.intellij.debugger.PositionManager;
import com.intellij.debugger.PositionManagerFactory;
import com.intellij.debugger.engine.DebugProcess;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class LiferayJspDebuggerPositionManagerFactory extends PositionManagerFactory {

	@Nullable
	@Override
	public PositionManager createPositionManager(@NotNull DebugProcess debugProcess) {
		return new LiferayJspDebuggerPositionManager(debugProcess);
	}

}