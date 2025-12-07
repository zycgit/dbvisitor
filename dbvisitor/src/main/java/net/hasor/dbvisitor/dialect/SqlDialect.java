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
import java.util.Set;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.builder.CommandBuilder;
import net.hasor.dbvisitor.lambda.core.OrderType;

/**
 * SQL 方言接口，定义数据库方言的基本功能
 * 功能特点：
 * 1. 提供数据库关键字识别
 * 2. 支持表名和列名的格式化
 * 3. 支持排序语法生成
 * 4. 支持批量操作
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface SqlDialect {
    /** Cannot be used as a key for column names. when column name is key words, Generate SQL using Qualifier warp it. */
    Set<String> keywords();

    /** 获取左限定符 */
    String leftQualifier();

    /** 获取有右限定符 */
    String rightQualifier();

    /** 列/表 别名的间隔符 */
    String aliasSeparator();

    /** 生成完整的表名 */
    String tableName(boolean useQualifier, String catalog, String schema, String table);

    /** 生成库名、Schema名、表名、列名 */
    String fmtName(boolean useQualifier, String name);

    /**
     * 生成默认排序语句
     * @param useQualifier 是否使用限定符
     * @param name 列名
     * @param orderType 排序类型
     * @return 排序语句
     */
    default String orderByDefault(boolean useQualifier, String name, String nameTerm, OrderType orderType) {
        String orderName = StringUtils.isBlank(nameTerm) ? this.fmtName(useQualifier, name) : nameTerm;
        switch (orderType) {
            case ASC:
                return orderName + " ASC";
            case DESC:
                return orderName + " DESC";
            case DEFAULT:
            default:
                return orderName;
        }
    }

    /**
     * 生成 NULL 值优先的排序语句
     * @param useQualifier 是否使用限定符
     * @param name 列名
     * @param orderType 排序类型
     * @return 排序语句
     */
    default String orderByNulls(boolean useQualifier, String name, String nameTerm, OrderType orderType) {
        return "";
    }

    /** 是否支持批量操作 */
    default boolean supportBatch() {
        return true;
    }

    default boolean supportGroupByAlias() {
        return false;
    }

    default boolean supportOrderByAlias() {
        return false;
    }

    CommandBuilder newBuilder();
}
