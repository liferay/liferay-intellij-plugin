/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.actions;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ScrollingModel;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

import com.liferay.ide.idea.bnd.psi.BndFile;
import com.liferay.ide.idea.bnd.psi.BndTokenType;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dominik Marks
 */
public class EnterInBndFileHandler extends EnterHandlerDelegateAdapter {

	@Override
	public Result preprocessEnter(
		@NotNull PsiFile file, @NotNull Editor editor, @NotNull Ref<Integer> caretOffsetRef,
		@NotNull Ref<Integer> caretAdvance, @NotNull DataContext dataContext, EditorActionHandler originalHandler) {

		if (file instanceof BndFile) {
			int caretOffset = caretOffsetRef.get();

			Document document = editor.getDocument();

			PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(file.getProject());

			psiDocumentManager.commitDocument(document);

			PsiElement psiAtOffset = file.findElementAt(caretOffset);

			_handleEnterInBndFile(editor, document, psiAtOffset, caretOffset);

			return Result.Stop;
		}

		return Result.Continue;
	}

	private void _handleEnterInBndFile(Editor editor, Document document, PsiElement psiAtOffset, int caretOffset) {
		String toInsert;

		String text = document.getText();

		String line = text.substring(0, caretOffset);

		line = line.trim();

		IElementType elementType = null;

		if (psiAtOffset != null) {
			ASTNode astNode = psiAtOffset.getNode();

			elementType = astNode.getElementType();
		}

		if (!line.endsWith("\\") &&
			((elementType == BndTokenType.HEADER_VALUE_PART) || (elementType == BndTokenType.COMMA) ||
			 line.endsWith(","))) {

			toInsert = "\\\n    ";
		}
		else {
			toInsert = "\n";
		}

		document.insertString(caretOffset, toInsert);

		caretOffset += toInsert.length();

		CaretModel caretModel = editor.getCaretModel();

		caretModel.moveToOffset(caretOffset);

		ScrollingModel scrollingModel = editor.getScrollingModel();

		scrollingModel.scrollToCaret(ScrollType.RELATIVE);

		SelectionModel selectionModel = editor.getSelectionModel();

		selectionModel.removeSelection();
	}

}