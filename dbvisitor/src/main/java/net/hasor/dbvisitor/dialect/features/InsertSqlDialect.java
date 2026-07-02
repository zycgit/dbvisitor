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
package net.hasor.dbvisitor.dialect.features;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;

/**
 * 插入 SQL 方言接口，扩展 {@link SqlDialect} 以支持多种插入操作
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface InsertSqlDialect extends SqlDialect {
    /** 根据本次 insert 上下文决定生成键回填场景下的执行策略。 */
    GeneratedKeyStrategy generatedKeyStrategy(List<String> primaryKey, List<String> columns, List<String> returnColumns, DuplicateKeyStrategy strategy);

    /** 是否支持指定的 insert 行为。 */
    boolean supportDuplicateStrategy(List<String> primaryKey, List<String> columns, List<String> returnColumns, DuplicateKeyStrategy strategy);

    /** 生成指定 insert 行为的 SQL 语句。 */
    String insertSql(DuplicateKeyStrategy duplicateStrategy, GeneratedKeyStrategy generatedStrategy, boolean useQualifier, String catalog, String schema, String table,//
            List<String> primaryKey, List<String> columns, List<String> returnColumns, int insertRows, Map<String, String> columnValueTerms);
}
