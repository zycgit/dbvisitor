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
import java.util.List;
import java.util.Map;

/**
 * 插入 SQL 方言接口，扩展 {@link SqlDialect} 以支持多种插入操作
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface InsertSqlDialect extends SqlDialect {
    /** 是否支持 insert into */
    boolean supportInto(List<String> primaryKey, List<String> columns);

    /** 生成标准 insert into 语句 */
    String insertInto(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms);

    /** 是否支持 insert ignore */
    boolean supportIgnore(List<String> primaryKey, List<String> columns);

    /** 生成 insert ignore 语句，根据具体数据库类型生成语句不一定是 insert 例如 oracle 会使用 merge 语句。 */
    String insertIgnore(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms);

    /** 是否支持 insert replace */
    boolean supportReplace(List<String> primaryKey, List<String> columns);

    /** 生成 insert replace 语句，根据具体数据库类型生成语句不一定是 insert 例如 oracle 会使用 merge 语句。 */
    String insertReplace(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms);
}
