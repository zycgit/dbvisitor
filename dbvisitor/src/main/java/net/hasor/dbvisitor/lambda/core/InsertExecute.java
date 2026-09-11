/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.core;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;

/**
 * lambda Insert 执行器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface InsertExecute<R, T> extends BasicFunc<R>, BoundSqlBuilder {
    /** 执行插入，并且将返回的int结果相加。 */
    default int executeSumResult() throws SQLException {
        int[] results = this.executeGetResult();
        return Arrays.stream(results).map(v -> switch (v) {
            case Statement.SUCCESS_NO_INFO -> 1;
            case Statement.EXECUTE_FAILED -> 0;
            default -> v;
        }).sum();
    }

    /** 执行插入，并返回所有结果 */
    int[] executeGetResult() throws SQLException;

    /** insert 策略，默认策略是 {@link DuplicateKeyStrategy#Into} */
    R onDuplicateStrategy(DuplicateKeyStrategy strategy);

    /** 批量插入记录 */
    default R applyEntity(T entity) throws SQLException {
        if (entity instanceof Map map) {
            return this.applyMap(Collections.singletonList((Map<String, Object>) map));
        } else {
            return this.applyEntity(Collections.singletonList(entity));
        }
    }

    /** 批量插入记录 */
    R applyEntity(T... entity) throws SQLException;

    /** 批量插入记录 */
    R applyEntity(List<T> entityList) throws SQLException;

    /** 批量插入记录 */
    default R applyMap(Map<String, Object> entity) throws SQLException {
        return applyMap(Collections.singletonList(entity));
    }

    /** 批量插入记录 */
    default R applyMap(Map<String, Object>... entity) throws SQLException {
        return applyMap(Arrays.asList(entity));
    }

    /** 批量插入记录 */
    R applyMap(List<Map<String, Object>> entityList) throws SQLException;
}
