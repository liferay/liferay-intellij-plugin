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

import com.intellij.debugger.JavaDebuggerBundle;
import com.intellij.debugger.NoDataException;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.JSR45PositionManager;
import com.intellij.ide.highlighter.JspxFileType;
import com.intellij.ide.highlighter.NewJspFileType;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.facet.JavaeeFacetUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class LiferayJspDebuggerPositionManager extends JSR45PositionManager<JavaeeFacet[]> {

	public LiferayJspDebuggerPositionManager(DebugProcess debugProcess) {
		super(
			debugProcess, JavaeeFacetUtil.getInstance().getAllJavaeeFacets(debugProcess.getProject()), "JSP",
			_LANGUAGE_FILE_TYPES, new LiferayJspDebuggerSourceFinderAdapter());
	}

	@NotNull
	@Override
	public List<Location> locationsOfLine(@NotNull ReferenceType referenceType, @NotNull SourcePosition sourcePosition)
		throws NoDataException {

		List<Location> locations = _locationsOfClassAt(referenceType, sourcePosition);

		if (locations != null) {
			return locations;
		}

		return Collections.emptyList();
	}

	@NonNls
	@Override
	protected String getGeneratedClassesPackage() {
		return "org.apache.jsp";
	}

	private void _checkSourcePositionFileType(final SourcePosition sourcePosition) throws NoDataException {
		PsiFile psiFile = sourcePosition.getFile();

		if (!getAcceptedFileTypes().contains(psiFile.getFileType())) {
			throw NoDataException.INSTANCE;
		}
	}

	private List<Location> _locationsOfClassAt(final ReferenceType referenceType, final SourcePosition sourcePosition)
		throws NoDataException {

		_checkSourcePositionFileType(sourcePosition);

		Application application = ApplicationManager.getApplication();

		return application.runReadAction(
			new Computable<List<Location>>() {

				@Override
				public List<Location> compute() {
					try {
						List<String> relativeSourcePaths = getRelativeSourePathsByType(referenceType);

						LiferayJspDebuggerSourceFinderAdapter sourcesFinder =
							(LiferayJspDebuggerSourceFinderAdapter)mySourcesFinder;

						for (String relativePath : relativeSourcePaths) {
							List<PsiFile> sourceFiles = sourcesFinder.findSourceFiles(
								relativePath, myDebugProcess.getProject(), myScope);

							for (PsiFile sourceFile : sourceFiles) {
								if ((sourceFile != null) && sourceFile.equals(sourcePosition.getFile())) {
									return getLocationsOfLine(
										referenceType, _getSourceName(sourceFile.getName(), referenceType),
										relativePath, sourcePosition.getLine() + 1);
								}
							}
						}
					}
					catch (AbsentInformationException | ClassNotPreparedException | ObjectCollectedException
								exception) {
					}
					catch (InternalError internalError) {
						myDebugProcess.printToConsole(
							JavaDebuggerBundle.message("internal.error.locations.of.line", referenceType.name()));
					}

					return null;
				}

				private String _getSourceName(String name, ReferenceType type) throws AbsentInformationException {
					List<String> sourceNames = type.sourceNames(getStratumId());

					Stream<String> stream = sourceNames.stream();

					return stream.filter(
						sourceName -> sourceName.contains(name)
					).findFirst(
					).orElse(
						name
					);
				}

			});
	}

	private static final LanguageFileType[] _LANGUAGE_FILE_TYPES = {NewJspFileType.INSTANCE, JspxFileType.INSTANCE};

}