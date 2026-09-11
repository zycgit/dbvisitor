/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.core;
/**
 * 排序策略。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-11-02
 */
public enum OrderType {
    /** 默认排序方式，行为由数据库决定。 */
    DEFAULT,
    /** 升序排序(ASC) */
    ASC,
    /** 降序排序(DESC) */
    DESC
}
