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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.server.gogo.GogoTelnetClient;
import com.liferay.ide.idea.util.LiferayIcons;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Properties;

/**
 * @author Terry Jia
 */
public class UninstallModuleAction extends AnAction {

	public UninstallModuleAction() {
		super("Uninstall", "uninstall module", LiferayIcons.LIFERAY_ICON);
	}

	public void actionPerformed(AnActionEvent event) {
		VirtualFile file = getVirtualFile(event);

		if (file == null) {
			return;
		}

		VirtualFile bndFile = file.findChild("bnd.bnd");

		if ((bndFile == null) || !bndFile.exists()) {
			return;
		}

		Properties properties = new Properties();

		try (InputStream in = Files.newInputStream(Paths.get(bndFile.getPath()))) {
			properties.load(in);

			String bsn = properties.getProperty("Bundle-SymbolicName");

			GogoTelnetClient client = new GogoTelnetClient("localhost", 11311);

			String cmd = "uninstall " + bsn;

			client.send(cmd);

			client.close();
		}
		catch (IOException ioe) {
		}
	}

	public boolean isEnabledAndVisible(AnActionEvent event) {
		VirtualFile file = getVirtualFile(event);

		if (file == null) {
			return false;
		}

		VirtualFile bndFile = file.findChild("bnd.bnd");

		if ((bndFile != null) && bndFile.exists()) {
			return true;
		}

		return false;
	}

	@Override
	public void update(AnActionEvent event) {
		super.update(event);

		Presentation eventPresentation = event.getPresentation();

		eventPresentation.setEnabledAndVisible(isEnabledAndVisible(event));
	}

	protected VirtualFile getVirtualFile(AnActionEvent event) {
		Object virtualFileObject = event.getData(CommonDataKeys.VIRTUAL_FILE);

		if ((virtualFileObject != null) && (virtualFileObject instanceof VirtualFile)) {
			return (VirtualFile)virtualFileObject;
		}

		return null;
	}

}