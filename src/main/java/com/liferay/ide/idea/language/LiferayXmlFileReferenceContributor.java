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

package com.liferay.ide.idea.language;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.SoftFileReferenceSet;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;

import org.jetbrains.annotations.NotNull;

/**
 * Reference contributor for file references in Liferay specific XML files
 *
 * @author Dominik Marks
 */
public class LiferayXmlFileReferenceContributor extends PsiReferenceContributor {

	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
		PsiElementPattern.Capture<PsiElement> capture = PlatformPatterns.psiElement(PsiElement.class);
		capture = capture.and(new LiferayXmlFileReferenceFilterPattern());

		registrar.registerReferenceProvider(
			capture,
			new PsiReferenceProvider() {

				@NotNull
				@Override
				public PsiReference[] getReferencesByElement(
					@NotNull PsiElement element, @NotNull ProcessingContext context) {

					TextRange originalRange = element.getTextRange();
					String valueString;
					int startInElement;

					if (element instanceof XmlAttributeValue) {
						TextRange valueRange = TextRange.create(1, originalRange.getLength() - 1);

						valueString = valueRange.substring(element.getText());

						startInElement = 1;
					}
					else {
						TextRange valueRange = TextRange.create(0, originalRange.getLength());

						valueString = valueRange.substring(element.getText());

						startInElement = 0;
					}

					FileReferenceSet fileReferenceSet = new SoftFileReferenceSet(
						valueString, element, startInElement, null, SystemInfo.isFileSystemCaseSensitive, false);

					return fileReferenceSet.getAllReferences();
				}

			});
	}

}