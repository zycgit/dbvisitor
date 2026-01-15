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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;

/**
 * MongoDB 方言实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-07
 */
public class MongoDialect extends AbstractBuilderDialect implements PageSqlDialect {
    public static final SqlDialect DEFAULT = new MongoDialect();

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
    public SqlCommandBuilder newBuilder() {
        return new MongoDialect();
    }

    // --- MongoDialect specific ---

    @Override
    public String tableName(boolean useQualifier, String catalog, String schema, String table) {
        String dbName = catalog;
        String collName = table;

        if (StringUtils.isBlank(catalog) && StringUtils.isNotBlank(schema)) {
            dbName = schema;
        }

        StringBuilder sb = new StringBuilder();
        if (StringUtils.isBlank(dbName)) {
            sb.append("db.");
        } else {
            sb.append(dbName).append(".");
        }
        sb.append(collName);
        return sb.toString();
    }

    @Override
    public String fmtName(boolean useQualifier, String name) {
        return name;
    }

    @Override
    public String aliasSeparator() {
        return ":";
    }

    @Override
    public String like(SqlLike likeType, Object value, String valueTerm) {
        if (StringUtils.isNotBlank(valueTerm)) {
            return valueTerm;
        }

        String strVal = value == null ? "" : value.toString();
        switch (likeType) {
            case LEFT:
                return escapeRegex("^", strVal, "");
            case RIGHT:
                return escapeRegex("", strVal, "$");
            default:
                return escapeRegex("", strVal, "");
        }
    }

    private String escapeRegex(String begin, String input, String end) {
        if (input == null) {
            return "";
        }
        if (!begin.isEmpty() && !StringUtils.startsWith(input, begin)) {
            input = begin + input;
        }
        if (!begin.isEmpty() && !StringUtils.endsWith(input, end)) {
            input = input + end;
        }
        return begin + input + end;
    }

    // --- Builder Implementation ---

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

    //

    @Override
    public void addCondition(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value, String valueTerm, SqlLike forLikeType) {
        this.conditions.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;

            if (type == ConditionType.LIKE) {
                String val = dialect.like(forLikeType != null ? forLikeType : SqlLike.DEFAULT, value, valueTerm);
                return field + ": { $regex: " + val + " }";
            }
            if (type == ConditionType.NOT_LIKE) {
                String val = dialect.like(forLikeType != null ? forLikeType : SqlLike.DEFAULT, value, valueTerm);
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
    public void addConditionGroup(ConditionLogic logic, Consumer<SqlCommandBuilder> group) {
        throw new UnsupportedOperationException("Mongo does not support nested conditions.");
    }

    //

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
        return selectAll;
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
    public void addUpdateSet(String col, Object value, String valueTerm) {
        this.updates.addSegment((delimited, dialect) -> {
            return col + ": " + formatValue(value, valueTerm);
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
    public boolean hasUpdateSet() {
        return !this.updates.isEmpty();
    }

    @Override
    public boolean hasInsert() {
        return !this.inserts.isEmpty();
    }

    private String formatValue(Object value, String valueTerm) {
        this.args.add(value);
        return StringUtils.isNotBlank(valueTerm) ? valueTerm : "?";
    }

    @Override
    public BoundSql buildSelect(boolean delimited) throws SQLException {
        MergeSqlSegment s = new MergeSqlSegment("");

        s.addSegment((d, dia) -> {
            return dia.tableName(d, catalog, null, collection) + ".find(";
        });

        // Filter
        s.addSegment((d, dia) -> "{");
        s.addSegment(conditions);
        s.addSegment((d, dia) -> "}");

        // Projection
        if (!projections.isEmpty() && !selectAll) {
            s.addSegment((d, dia) -> ", {");
            s.addSegment(projections);
            s.addSegment((d, dia) -> "}");
        }

        s.addSegment((d, dia) -> ")");

        // Sort
        if (!sorts.isEmpty()) {
            s.addSegment((d, dia) -> ".sort({");
            s.addSegment(sorts);
            s.addSegment((d, dia) -> "})");
        }

        String sqlString = s.getSqlSegment(delimited, this);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public BoundSql buildUpdate(boolean delimited, boolean allowEmptyWhere) throws SQLException {
        MergeSqlSegment s = new MergeSqlSegment("");

        s.addSegment((d, dia) -> {
            return dia.tableName(d, catalog, null, collection) + ".updateMany(";
        });

        // Filter
        s.addSegment((d, dia) -> "{");
        s.addSegment(conditions);
        s.addSegment((d, dia) -> "}");

        // Update
        s.addSegment((d, dia) -> ", { $set: {");
        s.addSegment(updates);
        s.addSegment((d, dia) -> "} }");

        s.addSegment((d, dia) -> ")");

        String sqlString = s.getSqlSegment(delimited, this);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public BoundSql buildDelete(boolean delimited, boolean allowEmptyWhere) throws SQLException {
        MergeSqlSegment s = new MergeSqlSegment("");

        s.addSegment((d, dia) -> dia.tableName(d, catalog, null, collection) + ".deleteMany(");

        // Filter
        s.addSegment((d, dia) -> "{");
        s.addSegment(conditions);
        s.addSegment((d, dia) -> "}");

        s.addSegment((d, dia) -> ")");

        String sqlString = s.getSqlSegment(delimited, this);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public BoundSql buildInsert(boolean delimited, List<String> primaryKey, DuplicateKeyStrategy strategy) throws SQLException {
        MergeSqlSegment s = new MergeSqlSegment("");

        s.addSegment((d, dia) -> dia.tableName(d, catalog, null, collection) + ".insertMany([");

        // Document
        s.addSegment((d, dia) -> "{");
        s.addSegment(inserts);
        s.addSegment((d, dia) -> "}");

        s.addSegment((d, dia) -> "])");

        String sqlString = s.getSqlSegment(delimited, this);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        return new BoundSql.BoundSqlObj("/*+overwrite_find_as_count*/" + boundSql.getSqlString(), boundSql.getArgs());
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sb = new StringBuilder("/*+");

        if (start <= 0) {
            sb.append("overwrite_find_limit=" + limit);
        } else {
            sb.append("overwrite_find_skip=" + start + ",overwrite_find_limit=" + limit);
        }

        sb.append("*/");
        return new BoundSql.BoundSqlObj(sb + boundSql.getSqlString(), boundSql.getArgs());
    }
}
