/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Christopher Bryan Boyd
 * @author Gregory Amerson
 */
public class SwitchConsumerTest {

	@Test
	public void testSwitchConsumerBuilder() throws Exception {
		StringBuilder output = new StringBuilder();

		SwitchConsumer.SwitchConsumerBuilder<String> switch_ = SwitchConsumer.newBuilder();

		SwitchConsumer<String> switchConsumer = switch_.addCase(
			s -> s.equals("foo"), s -> output.append("case1 " + s + "\n")
		).addCase(
			s -> s.equals("bar"), s -> output.append("case2 " + s + "\n")
		).setDefault(
			s -> output.append("default " + s + "\n")
		).build();

		Stream.of(
			_STRINGS
		).filter(
			s -> s != null
		).forEach(
			switchConsumer
		);

		Assert.assertEquals(_EXPECTED, output.toString());
	}

	@Test
	public void testTraditionalIfElseStatement() throws Exception {
		StringBuilder output = new StringBuilder();

		for (String s : _STRINGS) {
			if (s != null) {
				if (s.equals("foo")) {
					output.append("case1 " + s + "\n");
				}
				else if (s.equals("bar")) {
					output.append("case2 " + s + "\n");
				}
				else {
					output.append("default " + s + "\n");
				}
			}
		}

		Assert.assertEquals(_EXPECTED, output.toString());
	}

	private static final String _EXPECTED = "case1 foo\ncase2 bar\ndefault baz\ndefault quux\n";

	private static final String[] _STRINGS = {"foo", "bar", "baz", "quux"};

}