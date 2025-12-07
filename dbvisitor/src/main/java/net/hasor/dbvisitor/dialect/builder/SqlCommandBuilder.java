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
import java.util.*;
import java.util.function.Consumer;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.dbvisitor.dialect.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;
import net.hasor.dbvisitor.lambda.segment.Segment;

/**
 * SQL 命令构建器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-06
 */
public class SqlCommandBuilder implements CommandBuilder {
    private static final Segment              LEFT_PAREN  = (delimited, d) -> "(";
    private static final Segment              RIGHT_PAREN = (delimited, d) -> ")";
    //
    private final        List<Object>         args;
    private final        MergeSqlSegment      selectColumns;
    private final        Map<String, Segment> selectColumnsCache;
    private final        MergeSqlSegment      whereConditions;
    private final        MergeSqlSegment      groupByColumns;
    private final        MergeSqlSegment      orderByColumns;
    private              String               catalog;
    private              String               schema;
    private              String               table;
    //
    private              boolean              selectAll;
    //
    private              MergeSqlSegment      insertColumns;
    private              List<String>         insertColNames;
    private              Map<String, String>  insertColTerms;
    //
    private              MergeSqlSegment      insertValues;
    private              MergeSqlSegment      updateColumns;

    public SqlCommandBuilder() {
        this.args = new ArrayList<>();

        this.selectAll = false;
        this.selectColumns = new MergeSqlSegment(", ");
        this.selectColumnsCache = new HashMap<>();
        this.whereConditions = new MergeSqlSegment();
        this.groupByColumns = new MergeSqlSegment(", ");
        this.orderByColumns = new MergeSqlSegment(", ");
        this.insertColumns = new MergeSqlSegment(", ");
        this.insertColNames = new ArrayList<>();
        this.insertColTerms = new HashMap<>();
        this.insertValues = new MergeSqlSegment(", ");
        this.updateColumns = new MergeSqlSegment(", ");
    }

    @Override
    public void setTable(String catalog, String schema, String table) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
    }

    @Override
    public void addCondition(final ConditionLogic logic, final String col, final String colTerm, final ConditionType type, final Object value, final String valueTerm, final SqlLike forLikeType) {
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
            case IS_NULL:
                this.whereConditions.addSegment((d, dia) -> "IS NULL");
                break;
            case IS_NOT_NULL:
                this.whereConditions.addSegment((d, dia) -> "IS NOT NULL");
                break;
            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }

        if (type == ConditionType.LIKE) {
            this.whereConditions.addSegment((d, dia) -> formatLikeValue(dia, value, valueTerm, forLikeType));
        } else if (type != ConditionType.IS_NULL && type != ConditionType.IS_NOT_NULL) {
            this.whereConditions.addSegment((d, dia) -> formatValue(dia, value, valueTerm));
        }
    }

    @Override
    public void addConditionForBetween(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value1, String value1Term, Object value2, String value2Term) {
        appendConditionLogic(logic);
        this.whereConditions.addSegment((d, dia) -> formatColumn(d, dia, col, colTerm));
        this.whereConditions.addSegment((d, dia) -> "BETWEEN");
        this.whereConditions.addSegment((d, dia) -> formatValue(dia, value1, value1Term));
        this.whereConditions.addSegment((d, dia) -> "AND");
        this.whereConditions.addSegment((d, dia) -> formatValue(dia, value2, value2Term));
    }

    @Override
    public void addConditionForIn(ConditionLogic logic, String col, String colTerm, ConditionType type, Object[] values, String valueTerm) {
        appendConditionLogic(logic);
        this.whereConditions.addSegment((d, dia) -> formatColumn(d, dia, col, colTerm));
        this.whereConditions.addSegment((d, dia) -> "IN");
        this.whereConditions.addSegment((d, dia) -> formatInValue(dia, values, valueTerm));
    }

    @Override
    public void addConditionGroup(ConditionLogic logic, Consumer<CommandBuilder> group) {
        appendConditionLogic(logic);
        this.whereConditions.addSegment(LEFT_PAREN);
        group.accept(this);
        this.whereConditions.addSegment(RIGHT_PAREN);
    }

    private void appendConditionLogic(ConditionLogic logic) {
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

    private boolean isLastElementOpenParen() {
        if (this.whereConditions.isEmpty()) {
            return false;
        } else {
            return this.whereConditions.lastSegment() == LEFT_PAREN;
        }
    }

    private String formatColumn(boolean d, SqlDialect dia, String col, String colTerm) {
        return StringUtils.isNotBlank(colTerm) ? colTerm : dia.fmtName(d, col);
    }

    private String formatValue(SqlDialect dia, Object value, String valueTerm) {
        this.args.add(value);
        return StringUtils.isNotBlank(valueTerm) ? valueTerm : "?";
    }

    private String formatLikeValue(SqlDialect dia, Object value, String valueTerm, SqlLike likeType) {
        this.args.add(value);
        if (dia instanceof ConditionSqlDialect) {
            return ((ConditionSqlDialect) dia).like(likeType, value, valueTerm);
        } else {
            return StringUtils.isNotBlank(valueTerm) ? valueTerm : "?";
        }
    }

    private String formatInValue(SqlDialect dia, Object[] value, String valueTerm) {
        if (value == null || value.length == 0) {
            throw new IllegalArgumentException("value is empty.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < value.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(formatValue(dia, value[i], valueTerm));
        }
        sb.append(")");
        return sb.toString();
    }

    private void appendValue(Object value) {
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
    public void addSelect(String col, String colExpr, Object[] args) {
        Segment s = (d, dia) -> {
            Arrays.asList(args).forEach(this::appendValue);
            String colAlias = dia.fmtName(d, col);
            return colExpr + dia.aliasSeparator() + colAlias;
        };
        this.selectColumns.addSegment(s);
        this.selectColumnsCache.put(col, s);
    }

    @Override
    public boolean hasSelect(String col) {
        return this.selectAll || this.selectColumnsCache.containsKey(col);
    }

    @Override
    public boolean hasSelectAll() {
        return this.selectAll;
    }

    //

    @Override
    public void addGroupBy(String col) throws SQLException {
        if (!this.selectColumnsCache.containsKey(col)) {
            throw new SQLException("group by column not found.");
        }
        this.groupByColumns.addSegment((d, dia) -> {
            if (dia.supportGroupByAlias()) {
                return dia.fmtName(d, col); // same as addSelect colAlias
            } else {
                Segment s = this.selectColumnsCache.get(col);
                return s.getSqlSegment(d, dia);
            }
        });
    }

    @Override
    public void addOrderBy(String col, String colTerm, OrderType type, OrderNullsStrategy nullsStrategy) {
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
        this.insertColTerms.put(col, valueTerm);
    }

    //

    @Override
    public BoundSql buildSelect(SqlDialect dialect, boolean delimited) throws SQLException {
        MergeSqlSegment segment = new MergeSqlSegment();
        segment.addSegment((d, dia) -> "SELECT");
        if (this.selectColumns.isEmpty() || this.selectAll) {
            segment.addSegment((d, dia) -> "*");
        } else {
            segment.addSegment(this.selectColumns);
        }

        segment.addSegment((d, dia) -> "FROM");
        segment.addSegment((d, dia) -> dia.tableName(d, this.catalog, this.schema, this.table));

        buildWhere(segment, true);

        if (!this.groupByColumns.isEmpty()) {
            segment.addSegment((d, dia) -> "GROUP BY");
            segment.addSegment(this.groupByColumns);
        }
        if (!this.orderByColumns.isEmpty()) {
            segment.addSegment((d, dia) -> "ORDER BY");
            segment.addSegment(this.orderByColumns);
        }

        String sqlString = segment.getSqlSegment(delimited, dialect);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public BoundSql buildUpdate(SqlDialect dialect, boolean delimited, boolean allowEmptyWhere) throws SQLException {
        MergeSqlSegment segment = new MergeSqlSegment();
        segment.addSegment((d, dia) -> "UPDATE");
        segment.addSegment((d, dia) -> dia.tableName(d, this.catalog, this.schema, this.table));

        segment.addSegment((d, dia) -> "SET");
        segment.addSegment(this.updateColumns);

        buildWhere(segment, allowEmptyWhere);

        String sqlString = segment.getSqlSegment(delimited, dialect);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public BoundSql buildDelete(SqlDialect dialect, boolean delimited, boolean allowEmptyWhere) throws SQLException {
        MergeSqlSegment segment = new MergeSqlSegment();
        segment.addSegment((d, dia) -> "DELETE FROM");
        segment.addSegment((d, dia) -> dia.tableName(d, this.catalog, this.schema, this.table));

        buildWhere(segment, allowEmptyWhere);

        String sqlString = segment.getSqlSegment(delimited, dialect);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    private void buildWhere(MergeSqlSegment segment, boolean allowEmptyWhere) {
        if (!this.whereConditions.isEmpty()) {
            segment.addSegment((d, dia) -> "WHERE");
            segment.addSegment(this.whereConditions);
        } else if (!allowEmptyWhere) {
            throw new IllegalStateException("Where clause is empty.");
        }
    }

    @Override
    public BoundSql buildInsert(SqlDialect dialect, boolean delimited, List<String> primaryKey, DuplicateKeyStrategy strategy) throws SQLException {
        MergeSqlSegment segment = new MergeSqlSegment();

        if (dialect instanceof InsertSqlDialect) {
            InsertSqlDialect idia = ((InsertSqlDialect) dialect);
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
            segment.addSegment((d, dia) -> sql);
        } else {
            segment.addSegment((d, dia) -> "INSERT INTO");
            segment.addSegment((d, dia) -> dia.tableName(d, this.catalog, this.schema, this.table));
            segment.addSegment((d, dia) -> "(");
            segment.addSegment(this.insertColumns);
            segment.addSegment((d, dia) -> ")");
            segment.addSegment((d, dia) -> "VALUES");
            segment.addSegment((d, dia) -> "(");
            segment.addSegment(this.insertValues);
            segment.addSegment((d, dia) -> ")");
        }

        String sqlString = segment.getSqlSegment(delimited, dialect);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }
}
