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

package com.liferay.ide.idea.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author Ethan Sun
 */
public class MavenUtil {

	public static Model getMavenModel(File pomFile) throws IOException, XmlPullParserException {
		MavenXpp3Reader mavenReader = new MavenXpp3Reader();

		mavenReader.setAddDefaultEntities(true);

		return mavenReader.read(new FileReader(pomFile));
	}

	public static void updateMavenPom(Model model, File file) throws IOException {
		MavenXpp3Writer mavenWriter = new MavenXpp3Writer();

		FileWriter fileWriter = new FileWriter(file);

		mavenWriter.write(fileWriter, model);
	}

}