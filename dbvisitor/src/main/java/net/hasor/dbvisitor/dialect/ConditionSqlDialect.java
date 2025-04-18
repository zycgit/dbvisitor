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
package net.hasor.dbvisitor.dialect;
import net.hasor.cobble.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * SQL 条件方言
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface ConditionSqlDialect extends SqlDialect {
    /** like 查询相关的选项 */
    enum SqlLike {
        /** %值 */
        LEFT,
        /** 值% */
        RIGHT,
        /** %值% */
        DEFAULT
    }

    default String like(SqlLike likeType, Object value) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "%";
        }
        switch (likeType) {
            case LEFT:
                return "CONCAT('%', ? )";
            case RIGHT:
                return "CONCAT( ? ,'%')";
            default:
                return "CONCAT('%', ? ,'%')";
        }
    }

    default String randomQuery(boolean useQualifier, String catalog, String schema, String table, List<String> selectColumns, Map<String, String> columnTerms, int recordSize) {
        throw new UnsupportedOperationException();
    }
}
