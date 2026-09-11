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
import java.util.StringJoiner;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;
import net.hasor.dbvisitor.dialect.features.VectorSqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.lambda.core.MetricType;

/**
 * Milvus 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-02-02
 */
public class MilvusDialect extends AbstractSqlDialect implements PageSqlDialect, VectorSqlDialect, InsertSqlDialect {
    public static final SqlDialect DEFAULT = new MilvusDialect();

    @Override
    public SqlCommandBuilder newBuilder() {
        return new MilvusDialect();
    }

    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/milvus.keywords";
    }

    @Override
    public String tableName(boolean useQualifier, String catalog, String schema, String table) {
        if (StringUtils.isBlank(schema)) {
            return fmtName(useQualifier, table);
        } else {
            return fmtName(useQualifier, schema) + "." + fmtName(useQualifier, table);
        }
    }

    // --- InsertSqlDialect impl ---

    @Override
    public GeneratedKeyStrategy generatedKeyStrategy(List<String> primaryKey, List<String> columns, List<String> returnColumns, DuplicateKeyStrategy strategy) {
        return GeneratedKeyStrategy.OneByOne;
    }

    @Override
    public boolean supportDuplicateStrategy(List<String> primaryKey, List<String> columns, List<String> returnColumns, DuplicateKeyStrategy strategy) {
        if (strategy == null || strategy == DuplicateKeyStrategy.Into) {
            return true;
        }
        return strategy == DuplicateKeyStrategy.Update && primaryKey.size() == 1 && columns.contains(primaryKey.get(0));
    }

    @Override
    public String insertSql(DuplicateKeyStrategy strategy, GeneratedKeyStrategy generatedStrategy, boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, List<String> returnColumns, int insertRows, Map<String, String> columnValueTerms) {
        if (!supportDuplicateStrategy(primaryKey, columns, returnColumns, strategy)) {
            throw new UnsupportedOperationException("Milvus insert strategy requires Into or Update with an explicit primary key.");
        }
        StringJoiner names = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");
        for (String column : columns) {
            names.add(fmtName(useQualifier, column));
            String term = columnValueTerms == null ? null : columnValueTerms.get(column);
            values.add(StringUtils.isBlank(term) ? "?" : term);
        }
        String command = strategy == DuplicateKeyStrategy.Update ? "/*+ partial_update=true */ UPSERT INTO " : "INSERT INTO ";
        return command + tableName(useQualifier, catalog, schema, table) + " (" + names + ") VALUES (" + values + ")";
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        return new BoundSql.BoundSqlObj("/*+ overwrite_find_as_count=true */ " + boundSql.getSqlString(), boundSql.getArgs());
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sb = new StringBuilder(boundSql.getSqlString());
        if (limit > 0) {
            sb.append(" LIMIT ").append(limit);
        }
        if (start > 0) {
            sb.append(" OFFSET ").append(start);
        }
        return new BoundSql.BoundSqlObj(sb.toString(), boundSql.getArgs());
    }

    // --- VectorSqlDialect impl ---

    @Override
    public void addOrderByVector(String col, String colTerm, Object vector, String vectorTerm, MetricType metricType) {
        // first order by
        if (this.orderByColumns.isEmpty()) {
            this.whereConditions.addSegment((d, dia) -> "ORDER BY");
            this.whereConditions.addSegment(this.orderByColumns);
            this.lockWhere = true;
            this.lockGroupBy = true;
        }

        this.orderByColumns.addSegment((d, dia) -> {
            String orderByCol = formatColumn(d, dia, col, colTerm);
            String v = formatValue(dia, vector, vectorTerm);
            String operator;
            if (metricType == MetricType.COSINE) {
                operator = "<=>";
            } else if (metricType == MetricType.IP) {
                operator = "<#>";
            } else if (metricType == MetricType.HAMMING) {
                operator = "~=";
            } else if (metricType == MetricType.JACCARD) { // Jaccard
                operator = "<%>";
            } else if (metricType == MetricType.BM25) { // BM25
                operator = "<?>";
            } else {
                operator = "<->";
            }
            return orderByCol + " " + operator + " " + v;
        });
    }

    @Override
    public void addConditionForVectorRange(SqlCommandBuilder.ConditionLogic logic, String col, String colTerm, Object vector, String vectorTerm,//
            Object threshold, String thresholdTerm, MetricType metricType) {
        if (this.lockWhere) {
            throw new IllegalStateException("must before (group by/order by) invoke it.");
        }

        appendConditionLogic(logic);

        String operator;
        if (metricType == MetricType.COSINE) {
            operator = "<=>";
        } else if (metricType == MetricType.IP) {
            operator = "<#>";
        } else if (metricType == MetricType.HAMMING) {
            operator = "~=";
        } else if (metricType == MetricType.JACCARD) { // Jaccard
            operator = "<%>";
        } else if (metricType == MetricType.BM25) { // BM25
            operator = "<?>";
        } else {
            operator = "<->";
        }

        final String finalOperator = operator;
        this.whereConditions.addSegment((d, dia) -> formatColumn(d, dia, col, colTerm));
        this.whereConditions.addSegment((d, dia) -> finalOperator);
        this.whereConditions.addSegment((d, dia) -> formatValue(dia, vector, vectorTerm));
        // Milvus uses similarity scores for these metrics, not pgvector distances.
        boolean similarity = metricType == MetricType.COSINE || metricType == MetricType.IP || metricType == MetricType.BM25;
        this.whereConditions.addSegment((d, dia) -> similarity ? ">" : "<");
        this.whereConditions.addSegment((d, dia) -> formatValue(dia, threshold, thresholdTerm));
    }
}
