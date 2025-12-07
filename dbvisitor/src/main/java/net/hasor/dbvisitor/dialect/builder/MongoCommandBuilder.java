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
package net.hasor.dbvisitor.dialect.builder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;

/**
 * Mongo 命令构建器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-06
 */
public class MongoCommandBuilder implements CommandBuilder {
    private       String          catalog;
    private       String          collection;
    private final List<Object>    args        = new ArrayList<>();
    private final MergeSqlSegment conditions  = new MergeSqlSegment(", ");
    private final MergeSqlSegment projections = new MergeSqlSegment(", ");
    private final MergeSqlSegment sorts       = new MergeSqlSegment(", ");
    private final MergeSqlSegment updates     = new MergeSqlSegment(", ");
    private final MergeSqlSegment inserts     = new MergeSqlSegment(", ");
    private       boolean         selectAll   = false;

    @Override
    public void setTable(String catalog, String schema, String table) {
        this.catalog = catalog;
        if (StringUtils.isBlank(catalog) && StringUtils.isNotBlank(schema)) {
            this.catalog = schema;
        }
        this.collection = table;
    }

    @Override
    public void clearSelect() {
        this.projections.clear();
        this.selectAll = false;
    }

    @Override
    public void clearUpdateSet() {
        this.updates.clear();
    }

    @Override
    public void clearAll() {
        this.catalog = null;
        this.collection = null;
        this.args.clear();
        this.conditions.clear();
        this.projections.clear();
        this.sorts.clear();
        this.updates.clear();
        this.inserts.clear();
        this.selectAll = false;
    }

    @Override
    public void addCondition(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value, String valueTerm, SqlLike forLikeType) {
        this.conditions.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;

            if (type == ConditionType.LIKE) {
                String val;
                if (dialect instanceof ConditionSqlDialect) {
                    val = ((ConditionSqlDialect) dialect).like(forLikeType != null ? forLikeType : SqlLike.DEFAULT, value, valueTerm);
                } else {
                    val = formatValue(value, valueTerm);
                }
                return field + ": { $regex: " + val + " }";
            }
            if (type == ConditionType.NOT_LIKE) {
                String val;
                if (dialect instanceof ConditionSqlDialect) {
                    val = ((ConditionSqlDialect) dialect).like(forLikeType != null ? forLikeType : SqlLike.DEFAULT, value, valueTerm);
                } else {
                    val = formatValue(value, valueTerm);
                }
                return field + ": { $not: { $regex: " + val + " } }";
            }

            if (type == ConditionType.IS_NULL) {
                return field + ": null";
            }
            if (type == ConditionType.IS_NOT_NULL) {
                return field + ": { $ne: null }";
            }

            String val = formatValue(value, valueTerm);

            switch (type) {
                case EQ:
                    return field + ": " + val;
                case NE:
                    return field + ": { $ne: " + val + " }";
                case GT:
                    return field + ": { $gt: " + val + " }";
                case GE:
                    return field + ": { $gte: " + val + " }";
                case LT:
                    return field + ": { $lt: " + val + " }";
                case LE:
                    return field + ": { $lte: " + val + " }";
                default:
                    throw new UnsupportedOperationException("Unsupported type: " + type);
            }
        });
    }

    @Override
    public void addConditionForBetween(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value1, String value1Term, Object value2, String value2Term) {
        this.conditions.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            String val1 = formatValue(value1, value1Term);
            String val2 = formatValue(value2, value2Term);

            if (type == ConditionType.NOT_BETWEEN) {
                return "$or: [ { " + field + ": { $lt: " + val1 + " } }, { " + field + ": { $gt: " + val2 + " } } ]";
            } else {
                return field + ": { $gte: " + val1 + ", $lte: " + val2 + " }";
            }
        });
    }

    @Override
    public void addConditionForIn(ConditionLogic logic, String col, String colTerm, ConditionType type, Object[] values, String valueTerm) {
        this.conditions.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(formatValue(values[i], valueTerm));
            }
            sb.append("]");

            if (type == ConditionType.NOT_IN) {
                return field + ": { $nin: " + sb.toString() + " }";
            } else {
                return field + ": { $in: " + sb.toString() + " }";
            }
        });
    }

    @Override
    public void addRawCondition(ConditionLogic logic, BoundSql boundSql) {
        throw new UnsupportedOperationException("Mongo does not support raw SQL conditions.");
    }

    @Override
    public void addConditionGroup(ConditionLogic logic, Consumer<CommandBuilder> group) {
        throw new UnsupportedOperationException("Mongo does not support nested conditions.");
    }

    @Override
    public void addSelect(String col, String colTerm) {
        this.projections.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            return field + ": 1";
        });
    }

    @Override
    public void addSelectCustom(String colExpr, Object[] args) {
        throw new UnsupportedOperationException("Mongo does not support custom projections.");
    }

    @Override
    public void addSelectAll() {
        this.selectAll = true;
    }

    @Override
    public boolean hasSelect(String col) {
        return selectAll;// Simplified check - hard to check inside segments without rendering
    }

    @Override
    public boolean hasSelect() {
        return !this.projections.isEmpty() || this.selectAll;
    }

    @Override
    public void addGroupBy(String col, String colTerm) {
        throw new UnsupportedOperationException("GroupBy not supported in simple find command. Use aggregate.");
    }

    @Override
    public void addOrderBy(String col, String colTerm, OrderType type, OrderNullsStrategy nullsStrategy) {
        this.sorts.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            int dir = (type == OrderType.DESC) ? -1 : 1;
            return field + ": " + dir;
        });
    }

    @Override
    public void addUpdateSet(String col, String colTerm, Object value, String valueTerm) {
        this.updates.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            String val = formatValue(value, valueTerm);
            return field + ": " + val;
        });
    }

    @Override
    public void addInsert(String col, Object value, String valueTerm) {
        this.inserts.addSegment((delimited, dialect) -> {
            String val = formatValue(value, valueTerm);
            return col + ": " + val;
        });
    }

    @Override
    public BoundSql buildSelect(SqlDialect dialect, boolean delimited) throws SQLException {
        MergeSqlSegment segment = new MergeSqlSegment("");

        segment.addSegment((d, dia) -> {
            return dia.tableName(d, catalog, null, collection) + ".find(";
        });

        // Filter
        segment.addSegment((d, dia) -> "{");
        segment.addSegment(conditions);
        segment.addSegment((d, dia) -> "}");

        // Projection
        if (!projections.isEmpty() && !selectAll) {
            segment.addSegment((d, dia) -> ", {");
            segment.addSegment(projections);
            segment.addSegment((d, dia) -> "}");
        }

        segment.addSegment((d, dia) -> ")");

        // Sort
        if (!sorts.isEmpty()) {
            segment.addSegment((d, dia) -> ".sort({");
            segment.addSegment(sorts);
            segment.addSegment((d, dia) -> "})");
        }

        String sqlString = segment.getSqlSegment(delimited, dialect);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public BoundSql buildUpdate(SqlDialect dialect, boolean delimited, boolean allowEmptyWhere) throws SQLException {
        MergeSqlSegment segment = new MergeSqlSegment("");

        segment.addSegment((d, dia) -> {
            return dia.tableName(d, catalog, null, collection) + ".updateMany(";
        });

        // Filter
        segment.addSegment((d, dia) -> "{");
        segment.addSegment(conditions);
        segment.addSegment((d, dia) -> "}");

        // Update
        segment.addSegment((d, dia) -> ", { $set: {");
        segment.addSegment(updates);
        segment.addSegment((d, dia) -> "} }");

        segment.addSegment((d, dia) -> ")");

        String sqlString = segment.getSqlSegment(delimited, dialect);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public BoundSql buildDelete(SqlDialect dialect, boolean delimited, boolean allowEmptyWhere) throws SQLException {
        MergeSqlSegment segment = new MergeSqlSegment("");

        segment.addSegment((d, dia) -> dia.tableName(d, catalog, null, collection) + ".deleteMany(");

        // Filter
        segment.addSegment((d, dia) -> "{");
        segment.addSegment(conditions);
        segment.addSegment((d, dia) -> "}");

        segment.addSegment((d, dia) -> ")");

        String sqlString = segment.getSqlSegment(delimited, dialect);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public BoundSql buildInsert(SqlDialect dialect, boolean delimited, List<String> primaryKey, DuplicateKeyStrategy strategy) throws SQLException {
        MergeSqlSegment segment = new MergeSqlSegment("");

        segment.addSegment((d, dia) -> dia.tableName(d, catalog, null, collection) + ".insertMany([");

        // Document
        segment.addSegment((d, dia) -> "{");
        segment.addSegment(inserts);
        segment.addSegment((d, dia) -> "}");

        segment.addSegment((d, dia) -> "])");

        String sqlString = segment.getSqlSegment(delimited, dialect);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public boolean hasUpdateSet() {
        return !this.updates.isEmpty();
    }

    @Override
    public boolean hasInsert() {
        return !this.inserts.isEmpty();
    }

    private String formatValue(Object value, String valueTerm) {
        if (StringUtils.isNotBlank(valueTerm)) {
            return valueTerm;
        }
        this.args.add(value);
        return "?";
    }
}
