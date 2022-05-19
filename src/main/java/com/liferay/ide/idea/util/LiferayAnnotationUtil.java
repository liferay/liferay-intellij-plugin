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