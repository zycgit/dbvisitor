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
import java.util.stream.Collectors;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;
/**
 * SqlServer2005 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @since 2016-11-10
 */
public class SqlServerDialect extends AbstractSqlDialect implements PageSqlDialect, InsertSqlDialect {
    public static final SqlDialect DEFAULT = new SqlServerDialect();

    @Override
    public SqlCommandBuilder newBuilder() {
        return new SqlServerDialect();
    }

    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/sqlserver.keywords";
    }

    @Override
    public String leftQualifier() {
        return "[";
    }

    @Override
    public String rightQualifier() {
        return "]";
    }

    public String tableName(boolean useQualifier, String catalog, String schema, String table) {
        StringBuilder strBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(catalog)) {
            strBuilder.append(fmtName(useQualifier, catalog));
            strBuilder.append(".");
        }
        if (StringUtils.isNotBlank(schema)) {
            strBuilder.append(fmtName(useQualifier, schema));
            strBuilder.append(".");
        } else if (StringUtils.isNotBlank(catalog)) {
            strBuilder.append("dbo.");
        }

        strBuilder.append(fmtName(useQualifier, table));
        return strBuilder.toString();
    }

    // --- PageSqlDialect impl ---

    private static String getOrderByPart(String sql) {
        String loweredString = sql.toLowerCase();
        int orderByIndex = loweredString.indexOf("order by");
        if (orderByIndex != -1) {
            return sql.substring(orderByIndex);
        } else {
            return "";
        }
    }

    private static String removeOrderByPart(String sql) {
        String loweredString = sql.toLowerCase();
        int orderByIndex = loweredString.indexOf("order by");
        if (orderByIndex != -1) {
            return sql.substring(0, orderByIndex);
        } else {
            return sql;
        }
    }

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        return new BoundSql.BoundSqlObj("SELECT COUNT(*) FROM (" + removeOrderByPart(boundSql.getSqlString()) + ") as TEMP_T", boundSql.getArgs());
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        String sqlString = boundSql.getSqlString();
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));
        //
        StringBuilder pagingBuilder = new StringBuilder();
        String orderby = getOrderByPart(sqlString);
        String distinctStr = "";
        String sqlPartString = removeOrderByPart(sqlString).trim();
        String loweredPartString = sqlPartString.toLowerCase();
        if (loweredPartString.startsWith("select")) {
            int index = 6;
            if (loweredPartString.startsWith("select distinct")) {
                distinctStr = "DISTINCT ";
                index = 15;
            }
            sqlPartString = sqlPartString.substring(index);
        }
        pagingBuilder.append(sqlPartString);
        // if no ORDER BY is specified use fake ORDER BY field to avoid errors
        if (StringUtils.isBlank(orderby)) {
            orderby = "ORDER BY CURRENT_TIMESTAMP";
        }
        long firstParam = start + 1;
        long secondParam = start + limit;
        sqlString = "WITH selectTemp AS (SELECT " + distinctStr + "TOP 100 PERCENT " + //
                " ROW_NUMBER() OVER (" + orderby + ") as __row_number__, " + pagingBuilder + ") SELECT * FROM selectTemp WHERE __row_number__ BETWEEN " + firstParam + " AND " + secondParam + " ORDER BY __row_number__";
        return new BoundSql.BoundSqlObj(sqlString, paramArrays.toArray());
    }

    //

    public String randomQuery(boolean useQualifier, String catalog, String schema, String table, List<String> selectColumns, int recordSize) {
        String tableName = this.tableName(useQualifier, catalog, schema, table);
        StringBuilder select = new StringBuilder();

        if (selectColumns == null || selectColumns.isEmpty()) {
            select.append("*");
        } else {
            for (String col : selectColumns) {
                if (select.length() > 0) {
                    select.append(", ");
                }
                select.append(this.fmtName(useQualifier, col));
            }
        }

        return "select top " + recordSize + " " + select + " from " + tableName + " order by newid()";
    }

    // --- InsertSqlDialect impl ---

    @Override
    public boolean supportInto(List<String> primaryKey, List<String> columns) {
        return true;
    }

    @Override
    public String insertInto(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        return buildInsertSql("INSERT INTO ", useQualifier, catalog, schema, table, columns, columnValueTerms);
    }

    @Override
    public boolean supportIgnore(List<String> primaryKey, List<String> columns) {
        return primaryKey != null && !primaryKey.isEmpty();
    }

    @Override
    public String insertIgnore(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder sb = new StringBuilder();
        buildMergeInfoBasic(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms, sb);
        buildMergeInfoWhenNotMatched(useQualifier, columns, sb);
        return sb.toString();
    }

    @Override
    public boolean supportReplace(List<String> primaryKey, List<String> columns) {
        return primaryKey != null && !primaryKey.isEmpty();
    }

    @Override
    public String insertReplace(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder sb = new StringBuilder();
        buildMergeInfoBasic(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms, sb);
        buildMergeInfoWhenMatched(useQualifier, primaryKey, columns, sb);
        buildMergeInfoWhenNotMatched(useQualifier, columns, sb);
        return sb.toString();
    }

    private String buildInsertSql(String markString, boolean useQualifier, String catalog, String schema, String table, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder sb = new StringBuilder();
        sb.append(markString);
        sb.append(tableName(useQualifier, catalog, schema, table));
        sb.append(" (");
        appendColumnNames(sb, useQualifier, columns);
        sb.append(") VALUES (");
        appendValueTerms(sb, columns, columnValueTerms);
        sb.append(")");
        return sb.toString();
    }

    private void buildMergeInfoBasic(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms, StringBuilder sb) {
        sb.append("MERGE INTO ");
        sb.append(tableName(useQualifier, catalog, schema, table));
        sb.append(" AS TMP USING (VALUES (");
        appendValueTerms(sb, columns, columnValueTerms);
        sb.append(")) AS SRC(");
        appendColumnNames(sb, useQualifier, columns);
        sb.append(") ON ");
        for (int i = 0; i < primaryKey.size(); i++) {
            if (i > 0) {
                sb.append(" AND ");
            }
            String pkColumn = fmtName(useQualifier, primaryKey.get(i));
            sb.append("TMP.").append(pkColumn).append(" = SRC.").append(pkColumn);
        }
        sb.append(" ");
    }

    private void buildMergeInfoWhenNotMatched(boolean useQualifier, List<String> allColumns, StringBuilder sb) {
        sb.append("WHEN NOT MATCHED THEN INSERT (");
        appendColumnNames(sb, useQualifier, allColumns);
        sb.append(") VALUES (");
        appendSourceColumnNames(sb, useQualifier, allColumns);
        sb.append(");");
    }

    private void buildMergeInfoWhenMatched(boolean useQualifier, List<String> primaryKey, List<String> allColumns, StringBuilder sb) {
        List<String> updateColumns = allColumns.stream().filter(c -> !primaryKey.contains(c)).collect(Collectors.toList());
        if (updateColumns.isEmpty()) {
            return;
        }

        sb.append("WHEN MATCHED THEN UPDATE SET ");
        for (int i = 0; i < updateColumns.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            String column = fmtName(useQualifier, updateColumns.get(i));
            sb.append(column).append(" = SRC.").append(column);
        }
        sb.append(" ");
    }

    private void appendColumnNames(StringBuilder sb, boolean useQualifier, List<String> columns) {
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(fmtName(useQualifier, columns.get(i)));
        }
    }

    private void appendSourceColumnNames(StringBuilder sb, boolean useQualifier, List<String> columns) {
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("SRC.").append(fmtName(useQualifier, columns.get(i)));
        }
    }

    private void appendValueTerms(StringBuilder sb, List<String> columns, Map<String, String> columnValueTerms) {
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            String colName = columns.get(i);
            String valueTerm = columnValueTerms != null ? columnValueTerms.get(colName) : null;
            sb.append(StringUtils.isNotBlank(valueTerm) ? valueTerm : "?");
        }
    }
}
