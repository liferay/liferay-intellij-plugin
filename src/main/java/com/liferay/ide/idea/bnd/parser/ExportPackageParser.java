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

package com.liferay.ide.idea.bnd.parser;

import com.intellij.codeInsight.daemon.JavaErrorMessages;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.TokenSet;
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
								TextRange highlightTextRange = textRange.shiftRight(offset);

								annotationHolder.createErrorAnnotation(highlightTextRange, "Invalid reference");

								annotated = true;

								continue;
							}

							PsiDirectory[] psiDirectories = BndPsiUtil.resolvePackage(bndHeader, packageName);

							if (psiDirectories.length == 0) {
								TextRange textRangeWithoutWhitespaces = BndPsiUtil.adjustTextRangeWithoutWhitespaces(
									textRange, text);

								TextRange highlightTextRange = textRangeWithoutWhitespaces.shiftRight(offset);

								annotationHolder.createErrorAnnotation(
									highlightTextRange,
									JavaErrorMessages.message("cannot.resolve.package", packageName));

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
		else if (isUsesDirectiveAttributeOrDirective(parentPsiElement)) {
            List<PsiReference> psiReferences = ContainerUtil.newSmartList();

            ASTNode headerValuePartNode = bndHeaderValuePart.getNode();

            ASTNode[] childNodes = headerValuePartNode.getChildren(_tokenSet);

            for (ASTNode childNode : childNodes) {
                if (childNode instanceof BndToken) {
                    BndToken bndToken = (BndToken) childNode;

                    ContainerUtil.addAll(psiReferences, BndPsiUtil.getPackageReferences(bndToken));
                }
            }

            return psiReferences.toArray(new PsiReference[0]);
        }

		return PsiReference.EMPTY_ARRAY;
	}

    private boolean isUsesDirectiveAttributeOrDirective(PsiElement psiElement) {
        if (psiElement instanceof Attribute) {
            Attribute attribute = (Attribute)psiElement;

            return (Constants.USES_DIRECTIVE.equals(attribute.getName()));
        }
        if (psiElement instanceof Directive) {
            Directive directive = (Directive)psiElement;

            return (Constants.USES_DIRECTIVE.equals(directive.getName()));
        }

        return false;
    }

    private static final TokenSet _tokenSet = TokenSet.create(BndTokenType.HEADER_VALUE_PART);

}