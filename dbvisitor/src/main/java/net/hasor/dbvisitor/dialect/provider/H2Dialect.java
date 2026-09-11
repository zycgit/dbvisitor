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
 * H2 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class H2Dialect extends AbstractSqlDialect implements PageSqlDialect, SeqSqlDialect, InsertSqlDialect {
    public static final SqlDialect DEFAULT = new H2Dialect();

    @Override
    public SqlCommandBuilder newBuilder() {
        return new H2Dialect();
    }

    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/h2.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "\"";
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

    // --- SeqSqlDialect impl ---

    @Override
    public String selectSeq(boolean useQualifier, String catalog, String schema, String seqName) {
        StringBuilder sb = new StringBuilder("values next value for ");
        if (StringUtils.isNotBlank(schema)) {
            sb.append(fmtName(useQualifier, schema)).append(".");
        }
        sb.append(fmtName(useQualifier, seqName));
        return sb.toString();
    }

    // --- InsertSqlDialect impl ---

    @Override
    public GeneratedKeyStrategy generatedKeyStrategy(List<String> primaryKey, List<String> columns, List<String> returnColumns, DuplicateKeyStrategy strategy) {
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
        sb.append("MERGE INTO ");
        sb.append(tableName(useQualifier, catalog, schema, table));
        sb.append(" t USING (VALUES (");
        appendValueTerms(sb, columns, columnValueTerms);
        sb.append(")) s(");
        appendColumnNames(sb, useQualifier, columns);
        sb.append(") ON ");
        for (int i = 0; i < primaryKey.size(); i++) {
            if (i > 0) {
                sb.append(" AND ");
            }
            String key = fmtName(useQualifier, primaryKey.get(i));
            sb.append("t.").append(key).append("=s.").append(key);
        }
        sb.append(" WHEN NOT MATCHED THEN INSERT (");
        appendColumnNames(sb, useQualifier, columns);
        sb.append(") VALUES (");
        appendSourceColumnNames(sb, useQualifier, columns);
        sb.append(")");
        return sb.toString();
    }

    private String insertReplace(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder sb = new StringBuilder();
        sb.append("MERGE INTO ");
        sb.append(tableName(useQualifier, catalog, schema, table));
        sb.append(" (");
        appendColumnNames(sb, useQualifier, columns);
        sb.append(") KEY (");
        appendColumnNames(sb, useQualifier, primaryKey);
        sb.append(") VALUES (");
        appendValueTerms(sb, columns, columnValueTerms);
        sb.append(")");
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
            sb.append("s.").append(fmtName(useQualifier, columns.get(i)));
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
