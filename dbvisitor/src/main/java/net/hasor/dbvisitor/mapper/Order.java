/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper;

/**
 * 表示语句执行的时机的枚举类。
 * 可用于指定某个操作是在另一个操作之前执行还是之后执行。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-19
 */
public enum Order {
    /**
     * 表示操作在另一个操作之前执行。
     */
    Before,

    /**
     * 表示操作在另一个操作之后执行。
     */
    After
}
