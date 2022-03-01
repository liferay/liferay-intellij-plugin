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

package com.liferay.ide.idea.bnd.completion;

import com.intellij.codeInsight.completion.LightCompletionTestCase;

import org.junit.Test;

/**
 * @author Charles Wu
 */
public class BndFileCompletionTest extends LightCompletionTestCase {

	@Test
	public void testAttributeCompletion() {
		configureFromFileText("bnd.bnd", "Export-Package: org.osgi;ve<caret>\n");
		complete();
		checkResultByText("Export-Package: org.osgi;version=<caret>\n");
	}

	@Test
	public void testDirectiveCompletion() {
		configureFromFileText("bnd.bnd", "Export-Package: org.osgi;u<caret>\n");
		complete();
		checkResultByText("Export-Package: org.osgi;uses:=<caret>\n");
	}

	@Test
	public void testExportPackageCompletion() {
		configureFromFileText("bnd.bnd", "Export-Package: org.osgi;<caret>\n");
		complete();
		assertContainsItems("version", "uses");
	}

	@Test
	public void testHeaderCompletion() {
		configureFromFileText("bnd.bnd", "");
		complete();
		assertContainsItems("Import-Package", "Fragment-Host", "Export-Package", "Bundle-SymbolicName");
	}

	@Test
	public void testImportPackageCompletion() {
		configureFromFileText("bnd.bnd", "Import-Package: org.osgi;<caret>\n");
		complete();
		assertContainsItems("version", "resolution");
	}

	@Test
	public void testImportPackageResolutionCompletion() {
		configureFromFileText("bnd.bnd", "Import-Package: org.osgi;resolution:=<caret>\n");
		complete();
		assertContainsItems("mandatory", "optional");
	}

}