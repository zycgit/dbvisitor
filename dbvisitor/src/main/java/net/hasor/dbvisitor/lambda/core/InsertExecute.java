/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.lambda.core;

import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * lambda Insert 执行器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface InsertExecute<R, T> extends BasicFunc<R>, BoundSqlBuilder {
    /** 执行插入，并且将返回的int结果相加。 */
    default int executeSumResult() throws SQLException {
        int[] results = this.executeGetResult();
        return Arrays.stream(results).map(v -> {
            if (v == Statement.SUCCESS_NO_INFO) {
                return 1;
            } else if (v == Statement.EXECUTE_FAILED) {
                return 0;
            } else {
                return v;
            }
        }).sum();
    }

    /** 执行插入，并返回所有结果 */
    int[] executeGetResult() throws SQLException;

    /** insert 策略，默认策略是 {@link DuplicateKeyStrategy#Into} */
    R onDuplicateStrategy(DuplicateKeyStrategy strategy);

    /** 批量插入记录 */
    default R applyEntity(T entity) throws SQLException {
        return applyEntity(Collections.singletonList(entity));
    }

    /** 批量插入记录 */
    default R applyEntity(T... entity) throws SQLException {
        return applyEntity(Arrays.asList(entity));
    }

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
