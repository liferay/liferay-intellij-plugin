/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.formatting;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class BndFormattingTest extends BasePlatformTestCase {

	@Test
	public void testFormatter() {
		myFixture.configureByFiles("bnd.bnd");

		Project project = getProject();

		WriteCommandAction.Builder writeCommandAction = WriteCommandAction.writeCommandAction(project);

		writeCommandAction.run(
			() -> {
				CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);

				PsiFile psiFile = myFixture.getFile();

				codeStyleManager.reformatText(psiFile, Arrays.asList(psiFile.getTextRange()));
			});

		myFixture.checkResultByFile("formatted.bnd");
	}

	@Override
	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	private static final String _TEST_DATA_PATH = "testdata/com/liferay/ide/idea/bnd/formatting/BndFormattingTest";

}