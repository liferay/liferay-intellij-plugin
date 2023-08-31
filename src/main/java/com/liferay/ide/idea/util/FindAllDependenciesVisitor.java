/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;

/**
 * @author Terry Jia
 * @author Charles Wu
 */
public class FindAllDependenciesVisitor extends CodeVisitorSupport {

	public ListMultimap<String, GradleDependency> getDependencies() {
		return _dependencies;
	}

	/**
	 * parse "group:name:version:classifier"
	 */
	@Override
	public void visitArgumentlistExpression(ArgumentListExpression expression) {
		if (Objects.equals(_configurationName, "")) {
			super.visitArgumentlistExpression(expression);
		}
		else {
			String text = _getExpressionText(expression.getExpression(0));

			String[] groups = text.split(":");

			String version = (groups.length > 2) ? groups[2] : null;

			_dependencies.put(_configurationName, new GradleDependency(groups[0], groups[1], version));

			super.visitArgumentlistExpression(expression);
		}
	}

	@Override
	public void visitBlockStatement(BlockStatement block) {
		if (_dependenciesClosure) {
			_dependencyStatement = true;
			super.visitBlockStatement(block);

			_dependencyStatement = false;
		}
		else {
			super.visitBlockStatement(block);
		}
	}

	/**
	 * parse "configurationName group: group:, name: name, version: version"
	 */
	@Override
	public void visitMapExpression(MapExpression expression) {
		if (Objects.equals(_configurationName, "")) {
			super.visitMapExpression(expression);
		}
		else {
			Map<String, String> dependenceMap = new HashMap<>();

			for (MapEntryExpression mapEntryExpression : expression.getMapEntryExpressions()) {
				String key = _getExpressionText(mapEntryExpression.getKeyExpression());

				String value = _getExpressionText(mapEntryExpression.getValueExpression());

				dependenceMap.put(key, value);
			}

			_dependencies.put(_configurationName, new GradleDependency(dependenceMap));

			super.visitMapExpression(expression);
		}
	}

	@Override
	public void visitMethodCallExpression(MethodCallExpression call) {
		if (Objects.equals(call.getMethodAsString(), "dependencies")) {
			_dependenciesClosure = true;
			super.visitMethodCallExpression(call);

			_dependenciesClosure = false;
		}
		else if (Objects.equals(call.getMethodAsString(), "buildscript")) {
			super.visitMethodCallExpression(call);
		}
		else if (_dependenciesClosure && _dependencyStatement) {
			_configurationName = call.getMethodAsString();
			super.visitMethodCallExpression(call);
			_configurationName = "";
		}
	}

	private String _getExpressionText(Expression expression) {
		return expression.getText();
	}

	private String _configurationName = "";
	private ListMultimap<String, GradleDependency> _dependencies = MultimapBuilder.treeKeys(
	).arrayListValues(
	).build();
	private boolean _dependenciesClosure = false;
	private boolean _dependencyStatement = false;

}