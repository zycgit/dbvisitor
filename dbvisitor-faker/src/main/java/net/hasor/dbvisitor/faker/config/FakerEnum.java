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
package net.hasor.dbvisitor.faker.config;
/**
 * FakerTable 构建器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public enum FakerEnum {

    TABLE_CATALOG("catalog"),
    TABLE_SCHEMA("schema"),
    TABLE_TABLE("table"),
    TABLE_STRATEGY("strategy"),
    TABLE_DATA_LOADER("dataLoader"),
    TABLE_COLUMNS("columns"),
    TABLE_COL_IGNORE_ALL("ignoreColsAll"),
    TABLE_COL_IGNORE_INSERT("ignoreColsInsert"),
    TABLE_COL_IGNORE_UPDATE("ignoreColsUpdate"),
    TABLE_COL_IGNORE_WHERE("ignoreColsWhere"),

    COLUMN_SEED_FACTORY("seedFactory"),

    ;

    private final String configKey;

    public String getConfigKey() {
        return configKey;
    }

    FakerEnum(String configKey) {
        this.configKey = configKey;
    }
}