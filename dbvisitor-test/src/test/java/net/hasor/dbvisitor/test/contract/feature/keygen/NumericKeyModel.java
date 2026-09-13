/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.keygen;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Entity material for shared assertions over Integer and Long generated keys. */
public record NumericKeyModel<T>(Class<T> type, BiFunction<String, Integer, T> factory,
        Function<T, ? extends Number> key, BiConsumer<T, Long> assignKey) {
}
