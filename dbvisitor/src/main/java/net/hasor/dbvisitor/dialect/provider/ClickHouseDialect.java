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
package net.hasor.dbvisitor.dialect.provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;

/**
 * ClickHouse SqlDialect implementation.
 * @author zyc
 * @version 2026-06-22
 */
public class ClickHouseDialect extends AbstractSqlDialect implements PageSqlDialect, InsertSqlDialect {
    public static final SqlDialect DEFAULT = new ClickHouseDialect();

    @Override
    public SqlCommandBuilder newBuilder() {
        return new ClickHouseDialect();
    }

    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/clickhouse.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "`";
    }

    @Override
    public boolean supportGroupByAlias() {
        return true;
    }

    @Override
    public boolean supportOrderByAlias() {
        return true;
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sb = new StringBuilder(boundSql.getSqlString());
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));

        if (start <= 0) {
            sb.append(" LIMIT ?");
            paramArrays.add(limit);
        } else {
            sb.append(" LIMIT ?, ?");
            paramArrays.add(start);
            paramArrays.add(limit);
        }

        return new BoundSql.BoundSqlObj(sb.toString(), paramArrays.toArray());
    }

    // --- InsertSqlDialect impl ---

    @Override
    public boolean supportInto(List<String> primaryKey, List<String> columns) {
        return true;
    }

    @Override
    public String insertInto(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(tableName(useQualifier, catalog, schema, table));
        sb.append(" (");

        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i);
            if (i > 0) {
                sb.append(", ");
                argBuilder.append(", ");
            }

            sb.append(fmtName(useQualifier, colName));
            String valueTerm = columnValueTerms != null ? columnValueTerms.get(colName) : null;
            argBuilder.append(StringUtils.isNotBlank(valueTerm) ? valueTerm : "?");
        }

        sb.append(") VALUES (");
        sb.append(argBuilder);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean supportIgnore(List<String> primaryKey, List<String> columns) {
        return false;
    }

    @Override
    public String insertIgnore(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportReplace(List<String> primaryKey, List<String> columns) {
        return false;
    }

    @Override
    public String insertReplace(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        throw new UnsupportedOperationException();
    }
}
