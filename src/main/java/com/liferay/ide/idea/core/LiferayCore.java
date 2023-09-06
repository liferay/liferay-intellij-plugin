/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.core;

import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Simon Jiang
 */
public class LiferayCore {

	public static WorkspaceProvider getWorkspaceProvider(Project project) {
		Collection<WorkspaceProvider> workspaceProviders = _getWorkspaceProviders();

		for (WorkspaceProvider provider : workspaceProviders) {
			try {
				WorkspaceProvider workspaceProvider = provider.provide(project, WorkspaceProvider.class);

				if (workspaceProvider != null) {
					return workspaceProvider;
				}
			}
			catch (Throwable th) {
				throw new RuntimeException("getWorkspaceProvider error", th);
			}
		}

		return null;
	}

	private static Collection<WorkspaceProvider> _getWorkspaceProviders() {
		if (_workspaceProviders == null) {
			_workspaceProviders = new ArrayList<>();

			try {
				ServiceLoader<WorkspaceProvider> serviceLoader = ServiceLoader.load(
					WorkspaceProvider.class, LiferayCore.class.getClassLoader());

				Iterator<WorkspaceProvider> iterator = serviceLoader.iterator();

				while (iterator.hasNext()) {
					_workspaceProviders.add(iterator.next());
				}
			}
			catch (Throwable e) {
				Class<?> throwableClass = e.getClass();

				System.err.println(
					"Exception thrown while loading WorkspaceProvider." + System.lineSeparator() + "Exception: " +
						throwableClass.getName() + ": " + e.getMessage());

				Throwable cause = e.getCause();

				if (cause != null) {
					Class<?> throwableCauseClass = cause.getClass();

					System.err.print(throwableCauseClass.getName() + ": " + cause.getMessage());
				}
			}
		}

		return _workspaceProviders;
	}

	private static Collection<WorkspaceProvider> _workspaceProviders = null;

}