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
import net.hasor.dbvisitor.dialect.features.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;

/**
 * PostgreSQL 对象名有大小写敏感不敏感的问题
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class PostgreSqlDialect extends AbstractSqlDialect implements PageSqlDialect, InsertSqlDialect {
    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/postgresql.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "\"";
    }

    @Override
    public boolean supportGroupByAlias() {
        return true;
    }

    @Override
    public boolean supportOrderByAlias() {
        return true;
    }

    @Override
    public SqlCommandBuilder newBuilder() {
        return new PostgreSqlDialect();
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sb = new StringBuilder(boundSql.getSqlString());
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));

        if (limit > 0) {
            sb.append(" LIMIT ?");
            paramArrays.add(limit);
        }
        if (start > 0) {
            sb.append(" OFFSET ?");
            paramArrays.add(start);
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
        return buildSql("INSERT INTO ", useQualifier, catalog, schema, table, columns, columnValueTerms, "");
    }

    @Override
    public boolean supportIgnore(List<String> primaryKey, List<String> columns) {
        return true;
    }

    @Override
    public String insertIgnore(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        return buildSql("INSERT ", useQualifier, catalog, schema, table, columns, columnValueTerms, " ON CONFLICT DO NOTHING");
    }

    @Override
    public boolean supportReplace(List<String> primaryKey, List<String> columns) {
        return !primaryKey.isEmpty();
    }

    // 主键冲突更新非主键列
    @Override
    public String insertReplace(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        // ... ON CONFLICT (a) DO UPDATE SET (b, c, d) = (excluded.b, excluded.c, excluded.d);

        StringBuilder sb = new StringBuilder(" ON CONFLICT (");
        boolean first = true;
        for (String pk : primaryKey) {
            if (!first) {
                sb.append(", ");
            }

            sb.append(fmtName(useQualifier, pk));
            first = false;
        }
        sb.append(") DO UPDATE SET ");

        //ON CONFLICT (a) DO UPDATE SET (b, c, d) = (excluded.b, excluded.c, excluded.d);
        StringBuilder namesBuffer = new StringBuilder();
        StringBuilder updateBuffer = new StringBuilder();
        first = true;
        for (String col : columns) {
            if (!first) {
                sb.append(", ");
            }
            String wrapName = fmtName(useQualifier, col);
            namesBuffer.append(wrapName);
            updateBuffer.append("EXCLUDED.").append(wrapName);
            first = false;
        }
        sb.append("(" + namesBuffer + ") = (" + updateBuffer + ")");

        return buildSql("INSERT INTO ", useQualifier, catalog, schema, table, columns, columnValueTerms, sb.toString());
    }

    protected String buildSql(String markString, boolean useQualifier, String catalog, String schema, String table, List<String> columns, Map<String, String> columnValueTerms, String appendSql) {
        StringBuilder sb = new StringBuilder();
        sb.append(markString);
        sb.append(tableName(useQualifier, catalog, schema, table));
        sb.append(" ");
        sb.append("(");

        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i);
            if (i > 0) {
                sb.append(", ");
                argBuilder.append(", ");
            }

            sb.append(fmtName(useQualifier, colName));
            String valueTerm = columnValueTerms != null ? columnValueTerms.get(colName) : null;
            if (StringUtils.isNotBlank(valueTerm)) {
                argBuilder.append(valueTerm);
            } else {
                argBuilder.append("?");
            }
        }

        sb.append(") VALUES (");
        sb.append(argBuilder);
        sb.append(")");
        sb.append(appendSql);
        return sb.toString();
    }
}