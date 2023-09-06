/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;

import org.jetbrains.annotations.NotNull;

/**
 * @author Simon Jiang
 */
public class LiferayAnnotationUtil {

	public static void createAnnotation(
		AnnotationHolder annotationHolder, @NotNull HighlightSeverity severity, String message) {

		AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(severity, message);

		annotationBuilder.create();
	}

	public static void createAnnotation(
		AnnotationHolder annotationHolder, @NotNull HighlightSeverity severity, String message,
		@NotNull ProblemHighlightType highlightType) {

		AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(severity, message);

		annotationBuilder.highlightType(highlightType);

		annotationBuilder.create();
	}

	public static void createAnnotation(
		AnnotationHolder annotationHolder, @NotNull HighlightSeverity severity, String message,
		@NotNull TextAttributesKey textAttributes) {

		AnnotationBuilder annotationBuilder;

		if (StringUtil.isEmpty(message)) {
			annotationBuilder = annotationHolder.newSilentAnnotation(severity);
		}
		else {
			annotationBuilder = annotationHolder.newAnnotation(severity, message);
		}

		annotationBuilder.textAttributes(textAttributes);

		annotationBuilder.create();
	}

	public static void createAnnotation(
		AnnotationHolder annotationHolder, @NotNull HighlightSeverity severity, String message,
		@NotNull TextRange range) {

		AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(severity, message);

		annotationBuilder.range(range);

		annotationBuilder.create();
	}

	public static void createAnnotation(
		AnnotationHolder annotationHolder, @NotNull HighlightSeverity severity, String message,
		@NotNull TextRange range, @NotNull ProblemHighlightType highlightType) {

		AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(severity, message);

		annotationBuilder.range(range);

		annotationBuilder.highlightType(highlightType);

		annotationBuilder.create();
	}

}