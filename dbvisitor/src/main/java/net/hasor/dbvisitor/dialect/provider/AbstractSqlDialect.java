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
import java.util.*;
import java.util.function.Consumer;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.InsertSqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;
import net.hasor.dbvisitor.lambda.segment.Segment;

/**
 * 标准 SQL 构建器方言
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-06
 */
public abstract class AbstractSqlDialect extends AbstractBuilderDialect {
    public static final Segment              LEFT_PAREN  = (delimited, d) -> "(";
    public static final Segment              RIGHT_PAREN = (delimited, d) -> ")";
    //
    protected final     List<Object>         args;
    protected final     MergeSqlSegment      selectColumns;
    protected final     Map<String, Segment> selectColumnsCache;
    protected           boolean              selectAll;
    //
    protected final     MergeSqlSegment      whereConditions;
    protected           boolean              hasWhereConditions;
    protected final     MergeSqlSegment      groupByColumns;
    protected final     MergeSqlSegment      orderByColumns;
    private             String               catalog;
    private             String               schema;
    private             String               table;
    //
    private final       MergeSqlSegment      insertColumns;
    private final       List<String>         insertColNames;
    private final       List<Object>         insertColValues;
    private final       Map<String, String>  insertColTerms;
    //
    private final       MergeSqlSegment      insertValues;
    private final       MergeSqlSegment      updateColumns;
    //
    protected           boolean              lockWhere;
    protected           boolean              lockGroupBy;

    public AbstractSqlDialect() {
        this.args = new ArrayList<>();

        this.selectColumns = new MergeSqlSegment(", ");
        this.selectColumnsCache = new HashMap<>();
        this.selectAll = false;
        this.whereConditions = new MergeSqlSegment();
        this.hasWhereConditions = false;
        this.groupByColumns = new MergeSqlSegment(", ");
        this.orderByColumns = new MergeSqlSegment(", ");
        this.insertColumns = new MergeSqlSegment(", ");
        this.insertColNames = new ArrayList<>();
        this.insertColValues = new ArrayList<>();
        this.insertColTerms = new HashMap<>();
        this.insertValues = new MergeSqlSegment(", ");
        this.updateColumns = new MergeSqlSegment(", ");
        this.lockWhere = false;
        this.lockGroupBy = false;
    }

    @Override
    public void setTable(String catalog, String schema, String table) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
    }

    @Override
    public void clearSelect() {
        this.selectColumns.clear();
        this.selectColumnsCache.clear();
    }

    @Override
    public void clearUpdateSet() {
        this.updateColumns.clear();
    }

    @Override
    public void clearAll() {
        this.args.clear();
        this.selectColumns.clear();
        this.selectColumnsCache.clear();
        this.selectAll = false;
        this.whereConditions.clear();
        this.hasWhereConditions = false;
        this.groupByColumns.clear();
        this.orderByColumns.clear();
        this.catalog = null;
        this.schema = null;
        this.table = null;
        this.insertColumns.clear();
        this.insertColNames.clear();
        this.insertColValues.clear();
        this.insertColTerms.clear();
        this.insertValues.clear();
        this.updateColumns.clear();
        this.lockWhere = false;
        this.lockGroupBy = false;
    }

    @Override
    public void addCondition(final ConditionLogic logic, final String col, final String colTerm, final ConditionType type, final Object value, final String valueTerm, final SqlLike forLikeType) {
        if (this.lockWhere) {
            throw new IllegalStateException("must before (group by/order by) invoke it.");
        }

        appendConditionLogic(logic);
        this.whereConditions.addSegment((d, dia) -> formatColumn(d, dia, col, colTerm));
        switch (type) {
            case EQ:
                this.whereConditions.addSegment((d, dia) -> "=");
                break;
            case NE:
                this.whereConditions.addSegment((d, dia) -> "<>");
                break;
            case GT:
                this.whereConditions.addSegment((d, dia) -> ">");
                break;
            case GE:
                this.whereConditions.addSegment((d, dia) -> ">=");
                break;
            case LT:
                this.whereConditions.addSegment((d, dia) -> "<");
                break;
            case LE:
                this.whereConditions.addSegment((d, dia) -> "<=");
                break;
            case LIKE:
                this.whereConditions.addSegment((d, dia) -> "LIKE");
                break;
            case NOT_LIKE:
                this.whereConditions.addSegment((d, dia) -> "NOT LIKE");
                break;
            case IS_NULL:
                this.whereConditions.addSegment((d, dia) -> "IS NULL");
                break;
            case IS_NOT_NULL:
                this.whereConditions.addSegment((d, dia) -> "IS NOT NULL");
                break;
            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }

        if (type == ConditionType.LIKE || type == ConditionType.NOT_LIKE) {
            this.whereConditions.addSegment((d, dia) -> formatLikeValue(dia, value, valueTerm, forLikeType));
        } else if (type != ConditionType.IS_NULL && type != ConditionType.IS_NOT_NULL) {
            this.whereConditions.addSegment((d, dia) -> formatValue(dia, value, valueTerm));
        }
    }

    @Override
    public void addConditionForBetween(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value1, String value1Term, Object value2, String value2Term) {
        if (this.lockWhere) {
            throw new IllegalStateException("must before (group by/order by) invoke it.");
        }

        appendConditionLogic(logic);
        this.whereConditions.addSegment((d, dia) -> formatColumn(d, dia, col, colTerm));
        if (type == ConditionType.NOT_BETWEEN) {
            this.whereConditions.addSegment((d, dia) -> "NOT BETWEEN");
        } else {
            this.whereConditions.addSegment((d, dia) -> "BETWEEN");
        }
        this.whereConditions.addSegment((d, dia) -> formatValue(dia, value1, value1Term));
        this.whereConditions.addSegment((d, dia) -> "AND");
        this.whereConditions.addSegment((d, dia) -> formatValue(dia, value2, value2Term));
    }

    @Override
    public void addConditionForIn(ConditionLogic logic, String col, String colTerm, ConditionType type, Object[] values, String valueTerm) {
        if (this.lockWhere) {
            throw new IllegalStateException("must before (group by/order by) invoke it.");
        }

        appendConditionLogic(logic);
        this.whereConditions.addSegment((d, dia) -> formatColumn(d, dia, col, colTerm));
        if (type == ConditionType.NOT_IN) {
            this.whereConditions.addSegment((d, dia) -> "NOT IN");
        } else {
            this.whereConditions.addSegment((d, dia) -> "IN");
        }
        this.whereConditions.addSegment(LEFT_PAREN);
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                this.whereConditions.addSegment((d, dia) -> ",");
            }
            Object val = values[i];
            this.whereConditions.addSegment((d, dia) -> formatValue(dia, val, valueTerm));
        }
        this.whereConditions.addSegment(RIGHT_PAREN);
    }

    @Override
    public void addConditionGroup(ConditionLogic logic, Consumer<SqlCommandBuilder> group) {
        if (this.lockWhere) {
            throw new IllegalStateException("must before (group by/order by) invoke it.");
        }

        appendConditionLogic(logic);
        this.whereConditions.addSegment(LEFT_PAREN);
        group.accept(this);
        this.whereConditions.addSegment(RIGHT_PAREN);
    }

    @Override
    public void addRawCondition(ConditionLogic logic, BoundSql boundSql) {
        if (this.lockWhere) {
            throw new IllegalStateException("must before (group by/order by) invoke it.");
        }

        appendConditionLogic(logic);
        this.whereConditions.addSegment((d, dia) -> {
            String bsql = boundSql.getSqlString();
            Object[] barg = boundSql.getArgs();
            if (barg != null) {
                for (Object arg : barg) {
                    appendValue(arg);
                }
            }
            return bsql;
        });
    }

    protected void appendConditionLogic(ConditionLogic logic) {
        this.hasWhereConditions = true;

        final ConditionLogic colLogic = logic == null ? ConditionLogic.AND : logic;
        boolean needPrefix = !this.whereConditions.isEmpty() && !isLastElementOpenParen();
        if (needPrefix) {
            switch (colLogic) {
                case OR:
                case OR_NOT: {
                    this.whereConditions.addSegment((d, dia) -> "OR");
                    break;
                }
                default: {
                    this.whereConditions.addSegment((d, dia) -> "AND");
                    break;
                }
            }
        }

        if (colLogic == ConditionLogic.AND_NOT || colLogic == ConditionLogic.OR_NOT) {
            this.whereConditions.addSegment((d, dia) -> "NOT");
        }
    }

    protected boolean isLastElementOpenParen() {
        if (this.whereConditions.isEmpty()) {
            return false;
        } else {
            return this.whereConditions.lastSegment() == LEFT_PAREN;
        }
    }

    protected String formatColumn(boolean d, SqlDialect dia, String col, String colTerm) {
        return StringUtils.isNotBlank(colTerm) ? colTerm : dia.fmtName(d, col);
    }

    protected String formatValue(SqlDialect dia, Object value, String valueTerm) {
        this.args.add(value);
        return StringUtils.isNotBlank(valueTerm) ? valueTerm : "?";
    }

    protected String formatLikeValue(SqlDialect dia, Object value, String valueTerm, SqlLike likeType) {
        this.args.add(value);
        return dia.like(likeType, value, valueTerm);
    }

    protected void appendValue(Object value) {
        this.args.add(value);
    }

    //

    @Override
    public void addSelect(String col, String colTerm) {
        Segment s = (d, dia) -> {
            String colAlias = dia.fmtName(d, col);
            String colExpr = formatColumn(d, dia, col, colTerm);
            if (StringUtils.equals(colAlias, colExpr)) {
                return colAlias;
            } else {
                return colExpr + dia.aliasSeparator() + colAlias;
            }
        };
        this.selectColumns.addSegment(s);
        this.selectColumnsCache.put(col, s);
    }

    @Override
    public void addSelectCustom(String colExpr, Object[] args) {
        Segment s = (d, dia) -> {
            if (args != null) {
                Arrays.asList(args).forEach(this::appendValue);
            }
            return colExpr;
        };
        this.selectColumns.addSegment(s);
    }

    @Override
    public void addSelectAll() {
        this.selectAll = true;
    }

    @Override
    public boolean hasSelect(String col) {
        return this.selectColumnsCache.containsKey(col);
    }

    @Override
    public boolean hasSelect() {
        return !this.selectColumns.isEmpty();
    }

    //

    @Override
    public void addGroupBy(String col, String colTerm) {
        if (this.lockGroupBy) {
            throw new IllegalStateException("must before order by invoke it.");
        }

        // first group by
        if (this.groupByColumns.isEmpty()) {
            this.whereConditions.addSegment((d, dia) -> "GROUP BY");
            this.whereConditions.addSegment(this.groupByColumns);
            this.lockWhere = true;
        }

        this.groupByColumns.addSegment((d, dia) -> {
            if (this.selectColumnsCache.containsKey(col)) {
                if (dia.supportGroupByAlias()) {
                    return dia.fmtName(d, col); // same as addSelect colAlias
                } else {
                    Segment s = this.selectColumnsCache.get(col);
                    return s.getSqlSegment(d, dia);
                }
            } else {
                return formatColumn(d, dia, col, colTerm);
            }
        });
    }

    @Override
    public void addOrderBy(String col, String colTerm, OrderType type, OrderNullsStrategy nullsStrategy) {
        // first order by
        if (this.orderByColumns.isEmpty()) {
            this.whereConditions.addSegment((d, dia) -> "ORDER BY");
            this.whereConditions.addSegment(this.orderByColumns);
            this.lockWhere = true;
            this.lockGroupBy = true;
        }

        this.orderByColumns.addSegment((d, dia) -> {
            String orderByCol = dia.fmtName(d, col);
            Segment orderByTerm;
            if (dia.supportOrderByAlias() && this.selectColumnsCache.containsKey(col)) {
                orderByTerm = this.selectColumnsCache.get(col);
            } else {
                orderByTerm = (a, b) -> colTerm;
            }

            MergeSqlSegment segment = new MergeSqlSegment();
            if (nullsStrategy != null) {
                final OrderType strategyOrderType;
                switch (nullsStrategy) {
                    case FIRST: {
                        strategyOrderType = OrderType.DESC;
                        break;
                    }
                    case LAST: {
                        strategyOrderType = OrderType.ASC;
                        break;
                    }
                    case DEFAULT:
                    default: {
                        strategyOrderType = OrderType.DEFAULT;
                        break;
                    }
                }

                segment.addSegment((a, b) -> {
                    String nameTerm = orderByTerm.getSqlSegment(a, b);
                    String s = b.orderByNulls(a, orderByCol, nameTerm, strategyOrderType);
                    return StringUtils.isBlank(s) ? "" : (s + ",");
                });
            }

            segment.addSegment((a, b) -> {
                String nameTerm = orderByTerm.getSqlSegment(a, b);
                return b.orderByDefault(a, orderByCol, nameTerm, type);
            });

            return segment.getSqlSegment(d, dia);
        });
    }

    //

    @Override
    public void addUpdateSet(final String col, final Object value, final String valueTerm) {
        this.updateColumns.addSegment((d, dia) -> {
            String c = formatColumn(d, dia, col, null);
            if (value == null && StringUtils.isBlank(valueTerm)) {
                return c + " = NULL";
            } else {
                appendValue(value);
                String v = StringUtils.isNotBlank(valueTerm) ? valueTerm : "?";
                return c + (" = " + v);
            }
        });
    }

    @Override
    public void addInsert(String col, Object value, String valueTerm) {
        this.insertColumns.addSegment((d, dia) -> {
            return formatColumn(d, dia, col, null);
        });
        this.insertValues.addSegment((d, dia) -> {
            return formatValue(dia, value, valueTerm);
        });

        this.insertColNames.add(col);
        this.insertColValues.add(value);
        this.insertColTerms.put(col, valueTerm);
    }

    //

    @Override
    public BoundSql buildSelect(boolean delimited) throws SQLException {
        this.args.clear();
        MergeSqlSegment s = new MergeSqlSegment();
        s.addSegment((d, dia) -> "SELECT");
        if (this.selectColumns.isEmpty()) {
            s.addSegment((d, dia) -> "*");
        } else {
            s.addSegment(this.selectColumns);
        }

        s.addSegment((d, dia) -> "FROM");
        s.addSegment((d, dia) -> dia.tableName(d, this.catalog, this.schema, this.table));

        buildWhere(s, true, "SELECT");

        String sqlString = s.getSqlSegment(delimited, this);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public BoundSql buildUpdate(boolean delimited, boolean allowEmptyWhere) throws SQLException {
        this.args.clear();
        MergeSqlSegment s = new MergeSqlSegment();
        s.addSegment((d, dia) -> "UPDATE");
        s.addSegment((d, dia) -> dia.tableName(d, this.catalog, this.schema, this.table));

        s.addSegment((d, dia) -> "SET");
        s.addSegment(this.updateColumns);

        buildWhere(s, allowEmptyWhere, "UPDATE");

        String sqlString = s.getSqlSegment(delimited, this);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public BoundSql buildDelete(boolean delimited, boolean allowEmptyWhere) throws SQLException {
        this.args.clear();
        MergeSqlSegment s = new MergeSqlSegment();
        s.addSegment((d, dia) -> "DELETE FROM");
        s.addSegment((d, dia) -> dia.tableName(d, this.catalog, this.schema, this.table));

        buildWhere(s, allowEmptyWhere, "DELETE");

        String sqlString = s.getSqlSegment(delimited, this);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    private void buildWhere(MergeSqlSegment s, boolean allowEmptyWhere, String tips) {
        if (!this.whereConditions.isEmpty()) {
            if (this.hasWhereConditions) {
                s.addSegment((d, dia) -> "WHERE");
            }
            s.addSegment(this.whereConditions);
        } else if (!allowEmptyWhere) {
            throw new IllegalStateException("The dangerous " + tips + " operation, You must call `allowEmptyWhere()` to enable " + tips + " ALL.");
        }
    }

    @Override
    public BoundSql buildInsert(boolean delimited, List<String> primaryKey, DuplicateKeyStrategy strategy) throws SQLException {
        this.args.clear();
        MergeSqlSegment s = new MergeSqlSegment();

        if (this instanceof InsertSqlDialect) {
            InsertSqlDialect idia = ((InsertSqlDialect) this);
            String sql;
            switch (strategy == null ? DuplicateKeyStrategy.Into : strategy) {
                case Ignore:
                    if (idia.supportIgnore(primaryKey, this.insertColNames)) {
                        sql = idia.insertIgnore(delimited, this.catalog, this.schema, this.table, primaryKey, this.insertColNames, this.insertColTerms);
                    } else {
                        sql = idia.insertInto(delimited, this.catalog, this.schema, this.table, primaryKey, this.insertColNames, this.insertColTerms);
                    }
                    break;
                case Update:
                    if (idia.supportReplace(primaryKey, this.insertColNames)) {
                        sql = idia.insertReplace(delimited, this.catalog, this.schema, this.table, primaryKey, this.insertColNames, this.insertColTerms);
                    } else {
                        sql = idia.insertInto(delimited, this.catalog, this.schema, this.table, primaryKey, this.insertColNames, this.insertColTerms);
                    }
                    break;
                case Into:
                default:
                    sql = idia.insertInto(delimited, this.catalog, this.schema, this.table, primaryKey, this.insertColNames, this.insertColTerms);
                    break;
            }
            s.addSegment((d, dia) -> sql);
            this.args.addAll(this.insertColValues);
        } else {
            s.addSegment((d, dia) -> "INSERT INTO");
            s.addSegment((d, dia) -> dia.tableName(d, this.catalog, this.schema, this.table));
            s.addSegment(LEFT_PAREN);
            s.addSegment(this.insertColumns);
            s.addSegment(RIGHT_PAREN);
            s.addSegment((d, dia) -> "VALUES");
            s.addSegment(LEFT_PAREN);
            s.addSegment(this.insertValues);
            s.addSegment(RIGHT_PAREN);
        }

        String sqlString = s.getSqlSegment(delimited, this);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public boolean hasUpdateSet() {
        return !this.updateColumns.isEmpty();
    }

    @Override
    public boolean hasInsert() {
        return !this.insertColumns.isEmpty();
    }
}
