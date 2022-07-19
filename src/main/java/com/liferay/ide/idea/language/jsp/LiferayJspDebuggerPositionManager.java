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

package com.liferay.ide.idea.language.jsp;

import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.JSR45PositionManager;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.facet.JavaeeFacetUtil;
import com.intellij.jsp.highlighter.JspxFileType;
import com.intellij.jsp.highlighter.NewJspFileType;
import com.intellij.openapi.fileTypes.LanguageFileType;

import org.jetbrains.annotations.NonNls;

/**
 * @author Dominik Marks
 */
public class LiferayJspDebuggerPositionManager extends JSR45PositionManager<JavaeeFacet[]> {

	public LiferayJspDebuggerPositionManager(DebugProcess debugProcess) {
		super(
			debugProcess, JavaeeFacetUtil.getInstance().getAllJavaeeFacets(debugProcess.getProject()), "JSP",
			_LANGUAGE_FILE_TYPES, new LiferayJspDebuggerSourceFinderAdapter());
	}

	@NonNls
	@Override
	protected String getGeneratedClassesPackage() {
		return "org.apache.jsp";
	}

	private static final LanguageFileType[] _LANGUAGE_FILE_TYPES = {NewJspFileType.INSTANCE, JspxFileType.INSTANCE};

}