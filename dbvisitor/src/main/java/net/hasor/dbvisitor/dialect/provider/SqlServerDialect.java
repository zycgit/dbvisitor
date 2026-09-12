/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;
import net.hasor.dbvisitor.dialect.features.SeqSqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;

/**
 * SqlServer2005 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @since 2016-11-10
 */
public class SqlServerDialect extends AbstractSqlDialect implements PageSqlDialect, InsertSqlDialect, SeqSqlDialect {
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

    @Override
    public String selectSeq(boolean useQualifier, String catalog, String schema, String seqName) {
        return "SELECT NEXT VALUE FOR " + tableName(useQualifier, catalog, schema, seqName);
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
    public GeneratedKeyStrategy generatedKeyStrategy(List<String> primaryKey, List<String> columns, List<String> returnColumns, DuplicateKeyStrategy strategy) {
        if (!returnColumns.isEmpty()) {
            if (strategy == DuplicateKeyStrategy.Into) {
                return GeneratedKeyStrategy.MultiValuesResultSet;
            }
        }

        if (returnColumns.isEmpty()) {
            if (this.supportBatch()) {
                return GeneratedKeyStrategy.JdbcBatch;
            }
        }

        return GeneratedKeyStrategy.OneByOne;
    }

    @Override
    public boolean supportDuplicateStrategy(List<String> primaryKey, List<String> columns, List<String> returnColumns, DuplicateKeyStrategy strategy) {
        return switch (strategy == null ? DuplicateKeyStrategy.Into : strategy) {
            case Into -> true;
            case Ignore, Update -> primaryKey != null && !primaryKey.isEmpty();
        };
    }

    @Override
    public String insertSql(DuplicateKeyStrategy duplicateStrategy, GeneratedKeyStrategy generatedStrategy, boolean useQualifier, String catalog, String schema, String table,//
            List<String> primaryKey, List<String> columns, List<String> returnColumns, int insertRows, Map<String, String> columnValueTerms) {
        insertRows = generatedStrategy == GeneratedKeyStrategy.MultiValuesResultSet ? insertRows : 1;
        return switch (duplicateStrategy == null ? DuplicateKeyStrategy.Into : duplicateStrategy) {
            case Into -> this.insertInto(useQualifier, catalog, schema, table, columns, columnValueTerms, returnColumns, insertRows);
            case Ignore -> this.insertIgnore(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms);
            case Update -> this.insertReplace(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms);
        };
    }

    private String insertInto(boolean useQualifier, String catalog, String schema, String table, List<String> columns, Map<String, String> columnValueTerms, List<String> returnColumns, int insertRows) {
        return buildInsertSql("INSERT INTO ", useQualifier, catalog, schema, table, columns, columnValueTerms, returnColumns, insertRows);
    }

    private String insertIgnore(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder sb = new StringBuilder();
        buildMergeInfoBasic(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms, sb);
        buildMergeInfoWhenNotMatched(useQualifier, columns, sb);
        return sb.toString();
    }

    private String insertReplace(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder sb = new StringBuilder();
        buildMergeInfoBasic(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms, sb);
        buildMergeInfoWhenMatched(useQualifier, primaryKey, columns, sb);
        buildMergeInfoWhenNotMatched(useQualifier, columns, sb);
        return sb.toString();
    }

    private String buildInsertSql(String markString, boolean useQualifier, String catalog, String schema, String table,//
            List<String> columns, Map<String, String> columnValueTerms, List<String> returnColumns, int insertRows) {
        StringBuilder sb = new StringBuilder();
        sb.append(markString);
        sb.append(tableName(useQualifier, catalog, schema, table));
        sb.append(" (");
        appendColumnNames(sb, useQualifier, columns);
        sb.append(") ");

        if (CollectionUtils.isNotEmpty(returnColumns)) {
            sb.append("OUTPUT ");
            for (int i = 0; i < returnColumns.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("INSERTED.").append(fmtName(useQualifier, returnColumns.get(i)));
            }
            sb.append(" ");
        }

        sb.append("VALUES ");
        for (int i = 0; i < insertRows; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("(");
            appendValueTerms(sb, columns, columnValueTerms);
            sb.append(")");
        }
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
        List<String> updateColumns = allColumns.stream().filter(c -> !primaryKey.contains(c)).toList();
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
