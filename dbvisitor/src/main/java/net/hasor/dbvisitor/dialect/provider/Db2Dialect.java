/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.provider;
import java.util.List;
import java.util.Map;
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
 * DB2 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class Db2Dialect extends AbstractSqlDialect implements PageSqlDialect, SeqSqlDialect, InsertSqlDialect {
    public static final SqlDialect DEFAULT = new Db2Dialect();

    @Override
    public SqlCommandBuilder newBuilder() {
        return new Db2Dialect();
    }

    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/db2.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "\"";
    }

    @Override
    public String like(SqlLike likeType, Object value, String valueTerm) {
        valueTerm = StringUtils.isBlank(valueTerm) ? "?" : valueTerm.trim();
        return switch (likeType) {
            case LEFT -> "CONCAT('%', " + valueTerm + " )";
            case RIGHT -> "CONCAT( " + valueTerm + " ,'%')";
            case DEFAULT -> "CONCAT(CONCAT('%', " + valueTerm + " ) ,'%')";
        };
    }

    @Override
    public String selectSeq(boolean useQualifier, String catalog, String schema, String seqName) {
        StringBuilder sb = new StringBuilder("VALUES NEXT VALUE FOR ");
        if (StringUtils.isNotBlank(schema)) {
            sb.append(fmtName(useQualifier, schema)).append(".");
        }
        sb.append(fmtName(useQualifier, seqName));
        return sb.toString();
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS ROW_ID FROM ( ");
        sb.append(boundSql.getSqlString());
        sb.append(" ) AS TMP_PAGE) TMP_PAGE WHERE ROW_ID BETWEEN ? AND ?");

        Object[] paramArray = boundSql.getArgs();
        Object[] destArgs = new Object[paramArray.length + 2];
        System.arraycopy(paramArray, 0, destArgs, 0, paramArray.length);
        destArgs[paramArray.length] = start + 1;
        destArgs[paramArray.length + 1] = start + limit;
        return new BoundSql.BoundSqlObj(sb.toString(), destArgs);
    }

    // --- InsertSqlDialect impl ---

    @Override
    public GeneratedKeyStrategy generatedKeyStrategy(List<String> primaryKey, List<String> columns, List<String> returnColumns, DuplicateKeyStrategy strategy) {
        if (returnColumns.isEmpty()) {
            if (this.supportBatch()) {
                return GeneratedKeyStrategy.JdbcBatch;
            } else {
                return GeneratedKeyStrategy.OneByOne;
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
        return switch (duplicateStrategy == null ? DuplicateKeyStrategy.Into : duplicateStrategy) {
            case Into -> this.insertInto(useQualifier, catalog, schema, table, columns, columnValueTerms);
            case Ignore -> this.insertIgnore(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms);
            case Update -> this.insertReplace(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms);
        };
    }

    private String insertInto(boolean useQualifier, String catalog, String schema, String table, List<String> columns, Map<String, String> columnValueTerms) {
        return buildInsertSql("INSERT INTO ", useQualifier, catalog, schema, table, columns, columnValueTerms);
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
        sb.append(")");
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
