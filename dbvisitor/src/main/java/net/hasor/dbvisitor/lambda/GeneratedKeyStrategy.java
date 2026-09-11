/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;
/**
 * 批量插入时 generated key 的执行策略。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2026-07-02
 */
public enum GeneratedKeyStrategy {
    JdbcBatch,
    JdbcBatchGeneratedKeys,
    MultiValuesResultSet,
    OneByOne
}
