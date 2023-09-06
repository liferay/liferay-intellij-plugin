/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

/**
 * @author Ethan Sun
 */
public class MavenUtil {

	public static Model getMavenModel(File pomFile) throws IOException, XmlPullParserException {
		MavenXpp3Reader mavenReader = new MavenXpp3Reader();

		mavenReader.setAddDefaultEntities(true);

		return mavenReader.read(new FileReader(pomFile));
	}

	public static MavenProject getWorkspaceMavenProject(@NotNull Project project) {
		MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);

		Application application = ApplicationManager.getApplication();

		return application.runReadAction(
			new Computable<MavenProject>() {

				@Override
				public MavenProject compute() {
					return mavenProjectsManager.findContainingProject(
						LiferayWorkspaceSupport.getWorkspaceVirtualFile(project));
				}

			});
	}

	public static void updateMavenPom(Model model, File file) throws IOException {
		MavenXpp3Writer mavenWriter = new MavenXpp3Writer();

		FileWriter fileWriter = new FileWriter(file);

		mavenWriter.write(fileWriter, model);
	}

}