/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import com.google.common.collect.ListMultimap;

import java.io.File;
import java.io.IOException;

import java.util.List;

import org.apache.commons.io.FileUtils;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.builder.AstBuilder;

/**
 * @author Terry Jia
 */
public class GradleDependencyUpdater {

	public GradleDependencyUpdater(File file) throws IOException {
		this(FileUtils.readFileToString(file, "UTF-8"));

		_file = file;
	}

	public GradleDependencyUpdater(String scriptContents) {
		AstBuilder builder = new AstBuilder();

		_nodes = builder.buildFromString(scriptContents);
	}

	public ListMultimap<String, GradleDependency> getAllDependencies() {
		FindAllDependenciesVisitor visitor = new FindAllDependenciesVisitor();

		walkScript(visitor);

		return visitor.getDependencies();
	}

	public List<GradleDependency> getDependenciesByName(String configurationName) {
		ListMultimap<String, GradleDependency> allDependencies = getAllDependencies();

		return allDependencies.get(configurationName);
	}

	public void walkScript(GroovyCodeVisitor visitor) {
		for (ASTNode node : _nodes) {
			node.visit(visitor);
		}
	}

	private File _file;
	private List<ASTNode> _nodes;

}