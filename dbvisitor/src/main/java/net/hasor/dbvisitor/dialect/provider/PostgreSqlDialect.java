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
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL 对象名有大小写敏感不敏感的问题
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public class PostgreSqlDialect extends AbstractDialect implements PageSqlDialect, InsertSqlDialect {
    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/postgresql.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "\"";
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sqlBuilder = new StringBuilder(boundSql.getSqlString());
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));

        if (limit > 0) {
            sqlBuilder.append(" LIMIT ?");
            paramArrays.add(limit);
        }
        if (start > 0) {
            sqlBuilder.append(" OFFSET ?");
            paramArrays.add(start);
        }

        return new BoundSql.BoundSqlObj(sqlBuilder.toString(), paramArrays.toArray());
    }

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

        StringBuilder strBuffer = new StringBuilder(" ON CONFLICT (");
        boolean first = true;
        for (String pk : primaryKey) {
            if (!first) {
                strBuffer.append(", ");
            }

            strBuffer.append(fmtName(useQualifier, pk));
            first = false;
        }
        strBuffer.append(") DO UPDATE SET ");

        //ON CONFLICT (a) DO UPDATE SET (b, c, d) = (excluded.b, excluded.c, excluded.d);
        StringBuilder namesBuffer = new StringBuilder();
        StringBuilder updateBuffer = new StringBuilder();
        first = true;
        for (String col : columns) {
            if (!first) {
                strBuffer.append(", ");
            }
            String wrapName = fmtName(useQualifier, col);
            namesBuffer.append(wrapName);
            updateBuffer.append("EXCLUDED.").append(wrapName);
            first = false;
        }
        strBuffer.append("(" + namesBuffer + ") = (" + updateBuffer + ")");

        return buildSql("INSERT INTO ", useQualifier, catalog, schema, table, columns, columnValueTerms, strBuffer.toString());
    }

    protected String buildSql(String markString, boolean useQualifier, String catalog, String schema, String table, List<String> columns, Map<String, String> columnValueTerms, String appendSql) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(markString);
        strBuilder.append(tableName(useQualifier, catalog, schema, table));
        strBuilder.append(" ");
        strBuilder.append("(");

        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i);
            if (i > 0) {
                strBuilder.append(", ");
                argBuilder.append(", ");
            }

            strBuilder.append(fmtName(useQualifier, colName));
            String valueTerm = columnValueTerms != null ? columnValueTerms.get(colName) : null;
            if (StringUtils.isNotBlank(valueTerm)) {
                argBuilder.append(valueTerm);
            } else {
                argBuilder.append("?");
            }
        }

        strBuilder.append(") VALUES (");
        strBuilder.append(argBuilder);
        strBuilder.append(")");
        strBuilder.append(appendSql);
        return strBuilder.toString();
    }

    public String randomQuery(boolean useQualifier, String catalog, String schema, String table, List<String> selectColumns, Map<String, String> columnTerms, int recordSize) {
        String tableName = this.tableName(useQualifier, catalog, schema, table);
        StringBuilder select = new StringBuilder();

        if (selectColumns == null || selectColumns.isEmpty()) {
            select.append("*");
        } else {
            for (String col : selectColumns) {
                if (select.length() > 0) {
                    select.append(", ");
                }

                String valueTerm = columnTerms != null ? columnTerms.get(col) : null;
                if (StringUtils.isNotBlank(valueTerm)) {
                    select.append(valueTerm);
                } else {
                    select.append(this.fmtName(useQualifier, col));
                }
            }
        }

        return "select " + select + " from " + tableName + " order by random() limit " + recordSize;
    }
}
