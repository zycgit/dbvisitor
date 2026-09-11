/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.string;
/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-11-29
 */
public interface EnumOfValue<T> {
    int codeValue();

    T valueOfCode(int codeValue);
}
