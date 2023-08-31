/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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