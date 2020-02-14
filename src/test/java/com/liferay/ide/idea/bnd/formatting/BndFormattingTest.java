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

package com.liferay.ide.idea.bnd.formatting;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author Dominik Marks
 */
public class BndFormattingTest extends LightPlatformCodeInsightFixtureTestCase {

	public void testFormatter() {
		myFixture.configureByFiles("bnd.bnd");

		Project project = getProject();

		WriteCommandAction.Builder writeCommandAction = WriteCommandAction.writeCommandAction(project);

		writeCommandAction.run(
			() -> {
				CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);

				PsiFile psiFile = myFixture.getFile();

				TextRange textRange = psiFile.getTextRange();

				codeStyleManager.reformatText(myFixture.getFile(), ContainerUtil.newArrayList(textRange));
			});

		myFixture.checkResultByFile("formatted.bnd");
	}

	@Override
	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	private static final String _TEST_DATA_PATH = "testdata/com/liferay/ide/idea/bnd/formatting/BndFormattingTest";

}