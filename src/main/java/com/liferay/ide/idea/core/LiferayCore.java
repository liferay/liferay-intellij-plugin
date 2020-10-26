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