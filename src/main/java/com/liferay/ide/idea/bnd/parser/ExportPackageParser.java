/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.bnd.parser;

import com.intellij.codeInsight.daemon.JavaErrorBundle;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

import com.liferay.ide.idea.bnd.psi.Attribute;
import com.liferay.ide.idea.bnd.psi.BndHeader;
import com.liferay.ide.idea.bnd.psi.BndHeaderValue;
import com.liferay.ide.idea.bnd.psi.BndHeaderValuePart;
import com.liferay.ide.idea.bnd.psi.BndToken;
import com.liferay.ide.idea.bnd.psi.BndTokenType;
import com.liferay.ide.idea.bnd.psi.Clause;
import com.liferay.ide.idea.bnd.psi.Directive;
import com.liferay.ide.idea.bnd.psi.util.BndPsiUtil;
import com.liferay.ide.idea.util.LiferayAnnotationUtil;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import org.osgi.framework.Constants;

/**
 * @author Dominik Marks
 */
public class ExportPackageParser extends BasePackageParser {

	public static final ExportPackageParser INSTANCE = new ExportPackageParser();

	@Override
	public boolean annotate(@NotNull BndHeader bndHeader, @NotNull AnnotationHolder annotationHolder) {
		if (super.annotate(bndHeader, annotationHolder)) {
			return true;
		}

		boolean annotated = false;

		for (BndHeaderValue bndHeaderValue : bndHeader.getBndHeaderValues()) {
			if (bndHeaderValue instanceof Clause) {
				Clause clause = (Clause)bndHeaderValue;

				Directive usesDirective = clause.getDirective(Constants.USES_DIRECTIVE);

				if (usesDirective != null) {
					BndHeaderValuePart valueElement = usesDirective.getValueElement();

					if (valueElement != null) {
						String text = StringUtil.trimTrailing(valueElement.getText());

						int start;

						if (StringUtil.startsWithChar(text, '"')) {
							start = 1;
						}
						else {
							start = 0;
						}

						int length;

						if (StringUtil.endsWithChar(text, '"')) {
							length = text.length() - 1;
						}
						else {
							length = text.length();
						}

						int offset = valueElement.getTextOffset();

						while (start < length) {
							int end = text.indexOf(',', start);

							if (end < 0) {
								end = length;
							}

							TextRange textRange = new TextRange(start, end);

							start = end + 1;

							String packageName = textRange.substring(text);

							packageName = packageName.replaceAll("\\s", "");

							if (StringUtil.isEmptyOrSpaces(packageName)) {
								LiferayAnnotationUtil.createAnnotation(
									annotationHolder, HighlightSeverity.ERROR, "Invalid reference",
									textRange.shiftRight(offset));

								annotated = true;

								continue;
							}

							PsiDirectory[] psiDirectories = BndPsiUtil.resolvePackage(bndHeader, packageName);

							if (psiDirectories.length == 0) {
								TextRange textRangeWithoutWhitespaces = BndPsiUtil.adjustTextRangeWithoutWhitespaces(
									textRange, text);

								TextRange highlightTextRange = textRangeWithoutWhitespaces.shiftRight(offset);

								LiferayAnnotationUtil.createAnnotation(
									annotationHolder, HighlightSeverity.ERROR,
									JavaErrorBundle.message("cannot.resolve.package", packageName), highlightTextRange);

								annotated = true;
							}
						}
					}
				}
			}
		}

		return annotated;
	}

	@NotNull
	@Override
	public PsiReference[] getReferences(@NotNull BndHeaderValuePart bndHeaderValuePart) {
		PsiElement parentPsiElement = bndHeaderValuePart.getParent();

		if (parentPsiElement instanceof Clause) {
			PsiElement originalElement = bndHeaderValuePart.getOriginalElement();

			PsiElement prevSibling = originalElement.getPrevSibling();

			if (!(prevSibling instanceof BndToken)) {
				return BndPsiUtil.getPackageReferences(bndHeaderValuePart);
			}

			BndToken bndToken = (BndToken)prevSibling;

			if (bndToken.getTokenType() != BndTokenType.SEMICOLON) {
				return BndPsiUtil.getPackageReferences(bndHeaderValuePart);
			}
		}
		else if (_isUsesDirectiveAttributeOrDirective(parentPsiElement)) {
			List<PsiReference> psiReferences = new SmartList<>();

			ASTNode headerValuePartNode = bndHeaderValuePart.getNode();

			ASTNode[] childNodes = headerValuePartNode.getChildren(_tokenSet);

			for (ASTNode childNode : childNodes) {
				if (childNode instanceof BndToken) {
					BndToken bndToken = (BndToken)childNode;

					ContainerUtil.addAll(psiReferences, BndPsiUtil.getPackageReferences(bndToken));
				}
			}

			return psiReferences.toArray(new PsiReference[0]);
		}

		return PsiReference.EMPTY_ARRAY;
	}

	private boolean _isUsesDirectiveAttributeOrDirective(PsiElement psiElement) {
		if (psiElement instanceof Attribute) {
			Attribute attribute = (Attribute)psiElement;

			return Constants.USES_DIRECTIVE.equals(attribute.getName());
		}

		if (psiElement instanceof Directive) {
			Directive directive = (Directive)psiElement;

			return Constants.USES_DIRECTIVE.equals(directive.getName());
		}

		return false;
	}

	private static final TokenSet _tokenSet = TokenSet.create(BndTokenType.HEADER_VALUE_PART);

}