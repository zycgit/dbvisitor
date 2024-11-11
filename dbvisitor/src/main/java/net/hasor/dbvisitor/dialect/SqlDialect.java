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
import net.hasor.dbvisitor.wrapper.core.OrderType;

import java.util.Set;

/**
 * SQL 方言
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public interface SqlDialect {
    /** Cannot be used as a key for column names. when column name is key words, Generate SQL using Qualifier warp it. */
    Set<String> keywords();

    String leftQualifier();

    String rightQualifier();

    /** 生成 form 后面的表名 */
    String tableName(boolean useQualifier, String catalog, String schema, String table);

    String fmtName(boolean useQualifier, String name);

    default String orderByDefault(boolean useQualifier, String name, OrderType orderType) {
        switch (orderType) {
            case ASC:
                return this.fmtName(useQualifier, name) + " ASC";
            case DESC:
                return this.fmtName(useQualifier, name) + " DESC";
            case DEFAULT:
            default:
                return this.fmtName(useQualifier, name);
        }
    }

    default String orderByNullsFirst(boolean useQualifier, String name, OrderType orderType) {
        return this.orderByDefault(useQualifier, name, orderType);
    }

    default String orderByNullsLast(boolean useQualifier, String name, OrderType orderType) {
        return this.orderByDefault(useQualifier, name, orderType);
    }

    default boolean supportBatch() {
        return true;
    }
}
