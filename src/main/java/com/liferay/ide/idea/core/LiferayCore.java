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
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * @author Simon Jiang
 */
public class LiferayCore {

	public static WorkspaceProvider getWorkspaceProvider(Project project) {
		try {
			Collection<WorkspaceProvider> providers = _getWorkspaceProviders();

			for (WorkspaceProvider provider : providers) {
				try {
					WorkspaceProvider workspaceProvider = provider.provide(project, WorkspaceProvider.class);

					if (!Objects.isNull(workspaceProvider)) {
						return workspaceProvider;
					}
				}
				catch (Throwable th) {
					throw new RuntimeException("_getWorkspaceProvider error", th);
				}
			}
		}
		catch (Throwable th) {
			throw new RuntimeException("_getWorkspaceProvider error", th);
		}

		return null;
	}

	private static Collection<WorkspaceProvider> _getWorkspaceProviders() throws Exception {
		if (_workspaceProviders == null) {
			_workspaceProviders = new ArrayList<>();

			ServiceLoader<WorkspaceProvider> serviceLoader = ServiceLoader.load(
				WorkspaceProvider.class, LiferayCore.class.getClassLoader());

			Iterator<WorkspaceProvider> workspaceProviderIterator = serviceLoader.iterator();

			while (workspaceProviderIterator.hasNext()) {
				try {
					WorkspaceProvider workspaceProvider = workspaceProviderIterator.next();

					_workspaceProviders.add(workspaceProvider);
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

		return _workspaceProviders;
	}

	private static Collection<WorkspaceProvider> _workspaceProviders = null;

}