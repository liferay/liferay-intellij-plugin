/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.language.tag;

import com.intellij.psi.PsiReferenceProvider;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dominik Marks
 */
public class LiferayTaglibModelContextJavaBeanReferenceContributor extends AbstractLiferayTaglibReferenceContributor {

	@Override
	protected String[] getAttributeNames() {
		return new String[] {"name", "field"};
	}

	@Override
	protected PsiReferenceProvider getPsiReferenceProvider() {
		return new LiferayTaglibModelContextJavaBeanReferenceProvider();
	}

	@Override
	protected Map<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>> getTaglibAttributesMap() {
		return _taglibAttributes;
	}

	@SuppressWarnings("serial")
	private static Map<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>> _taglibAttributes =
		new HashMap<String, Collection<AbstractMap.SimpleImmutableEntry<String, String>>>() {
			{
				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_AUI,
					Arrays.asList(
						new AbstractMap.SimpleImmutableEntry<>("input", "field"),
						new AbstractMap.SimpleImmutableEntry<>("input", "name"),
						new AbstractMap.SimpleImmutableEntry<>("select", "field"),
						new AbstractMap.SimpleImmutableEntry<>("select", "name")));

				put(
					LiferayTaglibs.TAGLIB_URI_LIFERAY_UI,
					Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("input-field", "field")));
			}
		};

}