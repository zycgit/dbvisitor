/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.provider;
import java.sql.SQLException;
import java.util.*;
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
 * ClickHouse SqlDialect implementation.
 * @author zyc
 * @version 2026-06-22
 */
public class ClickHouseDialect extends AbstractSqlDialect implements PageSqlDialect, InsertSqlDialect, VectorSqlDialect {
    public enum DeleteMode {
        LIGHTWEIGHT,
        MUTATION
    }

    public static final SqlDialect DEFAULT = new ClickHouseDialect();
    private final       DeleteMode deleteMode;

    public ClickHouseDialect() {
        this(DeleteMode.LIGHTWEIGHT);
    }

    /** Select MUTATION for tables that support ALTER DELETE but not lightweight DELETE, such as Memory. */
    public ClickHouseDialect(DeleteMode deleteMode) {
        this.deleteMode = Objects.requireNonNull(deleteMode, "deleteMode");
    }

    @Override
    public SqlCommandBuilder newBuilder() {
        return new ClickHouseDialect(this.deleteMode);
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

    @Override
    public BoundSql buildUpdate(boolean delimited, boolean allowEmptyWhere) throws SQLException {
        return requireWhere(super.buildUpdate(delimited, allowEmptyWhere));
    }

    @Override
    public BoundSql buildDelete(boolean delimited, boolean allowEmptyWhere) throws SQLException {
        return requireWhere(super.buildDelete(delimited, allowEmptyWhere));
    }

    @Override
    protected String deleteFrom(String tableName) {
        return this.deleteMode == DeleteMode.MUTATION ? "ALTER TABLE " + tableName + " DELETE" : super.deleteFrom(tableName);
    }

    private BoundSql requireWhere(BoundSql sql) {
        // ClickHouse requires WHERE even when the caller explicitly permits all rows.
        if (this.whereConditions.isEmpty()) {
            return new BoundSql.BoundSqlObj(sql.getSqlString() + " WHERE 1 = 1", sql.getArgs());
        }
        return sql;
    }

    // --- VectorSqlDialect impl ---

    @Override
    public void addOrderByVector(String col, String colTerm, Object vector, String vectorTerm, MetricType metricType) {
        String function = distanceFunction(metricType);
        if (this.lockWhere) {
            throw new IllegalStateException("must before (group by/order by) invoke it.");
        }
        if (this.orderByColumns.isEmpty()) {
            this.whereConditions.addSegment((d, dia) -> "ORDER BY");
            this.whereConditions.addSegment(this.orderByColumns);
            this.lockWhere = true;
            this.lockGroupBy = true;
        }
        this.orderByColumns.addSegment((d, dia) -> function + "(" + formatColumn(d, dia, col, colTerm) + ", " + formatValue(dia, vector, vectorTerm) + ")");
    }

    @Override
    public void addConditionForVectorRange(ConditionLogic logic, String col, String colTerm, Object vector, String vectorTerm, Object threshold, String thresholdTerm, MetricType metricType) {
        String function = distanceFunction(metricType);
        if (this.lockWhere) {
            throw new IllegalStateException("must before (group by/order by) invoke it.");
        }
        appendConditionLogic(logic);
        this.whereConditions.addSegment((d, dia) -> function + "(" + formatColumn(d, dia, col, colTerm) + ", " + formatValue(dia, vector, vectorTerm) + ") < " + formatValue(dia, threshold, thresholdTerm));
    }

    private String distanceFunction(MetricType metricType) {
        return switch (metricType) {
            case L2 -> "L2Distance";
            case COSINE -> "cosineDistance";
            // Inner product is negated so ascending order returns the largest product first.
            case IP -> "-dotProduct";
            default -> throw new UnsupportedOperationException("ClickHouse vector metric is not supported: " + metricType);
        };
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
            case Ignore, Update -> false;
        };
    }

    @Override
    public String insertSql(DuplicateKeyStrategy duplicateStrategy, GeneratedKeyStrategy generatedStrategy, boolean useQualifier, String catalog, String schema, String table,//
            List<String> primaryKey, List<String> columns, List<String> returnColumns, int insertRows, Map<String, String> columnValueTerms) {
        insertRows = generatedStrategy == GeneratedKeyStrategy.MultiValuesResultSet ? insertRows : 1;
        return switch (duplicateStrategy == null ? DuplicateKeyStrategy.Into : duplicateStrategy) {
            case Into -> this.insertInto(useQualifier, catalog, schema, table, columns, columnValueTerms);
            case Ignore, Update -> throw new UnsupportedOperationException();
        };
    }

    private String insertInto(boolean useQualifier, String catalog, String schema, String table, List<String> columns, Map<String, String> columnValueTerms) {
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
}
