/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Christopher Bryan Boyd
 * @author Gregory Amerson
 */
public class SwitchConsumer<T> implements Consumer<T> {

	public static <T> SwitchConsumerBuilder<T> newBuilder() {
		return new SwitchConsumerBuilder<>();
	}

	public static <T> SwitchConsumerBuilder<T> newBuilder(Class<T> clazz) {
		return new SwitchConsumerBuilder<>();
	}

	public SwitchConsumer(Map<Predicate<T>, Consumer<T>> cases, Consumer<T> defaultConsumer) {
		_cases = cases;
		_defaultConsumer = defaultConsumer;
	}

	@Override
	public void accept(T t) {
		Set<Map.Entry<Predicate<T>, Consumer<T>>> set = _cases.entrySet();

		Stream<Map.Entry<Predicate<T>, Consumer<T>>> stream = set.stream();

		Optional<Map.Entry<Predicate<T>, Consumer<T>>> matchingCase = stream.filter(
			e -> {
				Predicate<T> predicate = e.getKey();

				return predicate.test(t);
			}
		).findFirst();

		if (matchingCase.isPresent()) {
			Map.Entry<Predicate<T>, Consumer<T>> value = matchingCase.get();

			Consumer<T> consumer = value.getValue();

			consumer.accept(t);
		}
		else if (_defaultConsumer != null) {
			_defaultConsumer.accept(t);
		}
	}

	public static class SwitchConsumerBuilder<T> {

		public SwitchConsumerBuilder<T> addCase(Predicate<T> p, Consumer<T> c) {
			_cases.put(p, c);

			return this;
		}

		public SwitchConsumer<T> build() {
			return new SwitchConsumer<>(_cases, _default);
		}

		public SwitchConsumerBuilder<T> setDefault(Consumer<T> c) {
			_default = c;

			return this;
		}

		private Map<Predicate<T>, Consumer<T>> _cases = new LinkedHashMap<>();
		private Consumer<T> _default;

	}

	private final Map<Predicate<T>, Consumer<T>> _cases;
	private final Consumer<T> _defaultConsumer;

}