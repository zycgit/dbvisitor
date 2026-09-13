/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.provider;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;
import net.hasor.dbvisitor.lambda.segment.Segment;
import net.hasor.dbvisitor.types.SqlArg;

/**
 * MongoDB 方言实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-07
 */
public class MongoDialect extends AbstractBuilderDialect implements PageSqlDialect, InsertSqlDialect {
    public static final SqlDialect DEFAULT = new MongoDialect();

    private       String               catalog;
    private       String               collection;
    private final List<Object>         args              = new ArrayList<>();
    private final MergeSqlSegment      conditions        = new MergeSqlSegment(", ");
    private final List<ConditionLogic> conditionLogics   = new ArrayList<>();
    private final Set<String>          selectedColumns   = new HashSet<>();
    private final Set<String>          sortedColumns     = new HashSet<>();
    private final Map<String, String>  groupedColumns    = new LinkedHashMap<>();
    private final MergeSqlSegment      customProjections = new MergeSqlSegment(", ");
    private       String               customPipeline;
    private final MergeSqlSegment      projections       = new MergeSqlSegment(", ");
    private final MergeSqlSegment      sorts             = new MergeSqlSegment(", ");
    private final MergeSqlSegment      updates           = new MergeSqlSegment(", ");
    private final MergeSqlSegment      inserts           = new MergeSqlSegment(", ");
    private final List<String>         insertedColumns   = new ArrayList<>();
    private       boolean              selectAll         = false;

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

        Object rawValue = value instanceof SqlArg ? ((SqlArg) value).getValue() : value;
        String strVal = rawValue == null ? "" : rawValue.toString();
        return switch (likeType) {
            case LEFT -> escapeRegex("", strVal, "$");
            case RIGHT -> escapeRegex("^", strVal, "");
            default -> escapeRegex("", strVal, "");
        };
    }

    private String escapeRegex(String begin, String input, String end) {
        StringBuilder pattern = new StringBuilder(begin);
        StringBuilder literal = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (current == '\\' && i + 1 < input.length()) {
                literal.append(input.charAt(++i));
            } else if (current == '%' || current == '_') {
                appendRegexLiteral(pattern, literal);
                pattern.append(current == '%' ? "[\\s\\S]*" : "[\\s\\S]");
            } else {
                literal.append(current);
            }
        }
        appendRegexLiteral(pattern, literal);
        return pattern.append(end).toString();
    }

    private void appendRegexLiteral(StringBuilder pattern, StringBuilder literal) {
        if (literal.length() > 0) {
            pattern.append(Pattern.quote(literal.toString()));
            literal.setLength(0);
        }
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
        this.customProjections.clear();
        this.customPipeline = null;
        this.selectedColumns.clear();
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
        this.conditionLogics.clear();
        this.selectedColumns.clear();
        this.projections.clear();
        this.sorts.clear();
        this.groupedColumns.clear();
        this.customProjections.clear();
        this.customPipeline = null;
        this.sortedColumns.clear();
        this.updates.clear();
        this.inserts.clear();
        this.insertedColumns.clear();
        this.selectAll = false;
    }

    //

    @Override
    public void addCondition(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value, String valueTerm, SqlLike forLikeType) {
        addFilter(logic, (delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;

            if (type == ConditionType.LIKE) {
                String val = formatLikeValue(forLikeType, value, valueTerm);
                return field + ": { $regex: " + val + " }";
            }
            if (type == ConditionType.NOT_LIKE) {
                String val = formatLikeValue(forLikeType, value, valueTerm);
                return field + ": { $not: { $regex: " + val + " } }";
            }

            if (type == ConditionType.IS_NULL) {
                return field + ": null";
            }
            if (type == ConditionType.IS_NOT_NULL) {
                return field + ": { $ne: null }";
            }

            String val = formatValue(value, valueTerm);

            return switch (type) {
                case EQ -> field + ": " + val;
                case NE -> field + ": { $ne: " + val + " }";
                case GT -> field + ": { $gt: " + val + " }";
                case GE -> field + ": { $gte: " + val + " }";
                case LT -> field + ": { $lt: " + val + " }";
                case LE -> field + ": { $lte: " + val + " }";
                default -> throw new UnsupportedOperationException("Unsupported type: " + type);
            };
        });
    }

    @Override
    public void addConditionForBetween(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value1, String value1Term, Object value2, String value2Term) {
        addFilter(logic, (delimited, dialect) -> {
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
        addFilter(logic, (delimited, dialect) -> {
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
        String filter = boundSql.getSqlString().trim();
        if (filter.isEmpty()) {
            return;
        }
        String body = filter.startsWith("{") && filter.endsWith("}") ? filter.substring(1, filter.length() - 1).trim() : filter;
        if (body.isEmpty()) {
            return;
        }
        addFilter(logic, (delimited, dialect) -> {
            if (boundSql.getArgs() != null) {
                this.args.addAll(Arrays.asList(boundSql.getArgs()));
            }
            return body;
        });
    }

    @Override
    public void addConditionGroup(ConditionLogic logic, Consumer<SqlCommandBuilder> group) {
        MongoDialect nested = new MongoDialect();
        group.accept(nested);
        if (!nested.conditions.isEmpty()) {
            addFilter(logic, (delimited, dialect) -> {
                nested.args.clear();
                String filter = nested.renderConditions(delimited);
                this.args.addAll(nested.args);
                return filter;
            });
        }
    }

    private void addFilter(ConditionLogic logic, Segment filter) {
        this.conditionLogics.add(logic);
        this.conditions.addSegment(filter);
    }

    // Keep repeated fields in separate documents and retain AND precedence over OR.
    private String renderConditions(boolean delimited) throws SQLException {
        List<String> alternatives = new ArrayList<>();
        List<String> conjunction = new ArrayList<>();
        for (int i = 0; i < this.conditions.size(); i++) {
            ConditionLogic logic = this.conditionLogics.get(i);
            if (!conjunction.isEmpty() && (logic == ConditionLogic.OR || logic == ConditionLogic.OR_NOT)) {
                alternatives.add(joinConditions("$and", conjunction));
                conjunction.clear();
            }
            String filter = this.conditions.getSqlSegment(i).getSqlSegment(delimited, this);
            if (logic == ConditionLogic.AND_NOT || logic == ConditionLogic.OR_NOT) {
                filter = "$nor: [{" + filter + "}]";
            }
            conjunction.add(filter);
        }
        if (!conjunction.isEmpty()) {
            alternatives.add(joinConditions("$and", conjunction));
        }
        return joinConditions("$or", alternatives);
    }

    private String joinConditions(String operator, List<String> filters) {
        if (filters.isEmpty()) {
            return "";
        }
        if (filters.size() == 1) {
            return filters.get(0);
        }
        return operator + ": [{" + String.join("}, {", filters) + "}]";
    }

    //

    @Override
    public void addSelect(String col, String colTerm) {
        this.selectedColumns.add(StringUtils.isNotBlank(colTerm) ? colTerm : col);
        this.projections.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            return field + ": 1";
        });
    }

    @Override
    public void addSelectCustom(String colExpr, Object[] args) {
        if (args != null && args.length > 0) {
            throw new IllegalArgumentException("MongoDB custom projections do not accept positional arguments.");
        }
        String expression = colExpr.trim();
        if (this.customPipeline != null) {
            throw new IllegalArgumentException("A MongoDB pipeline cannot be combined with other projections.");
        }

        if (expression.startsWith("[") && expression.endsWith("]")) {
            if (!this.customProjections.isEmpty() || !this.projections.isEmpty()) {
                throw new IllegalArgumentException("A MongoDB pipeline cannot be combined with other projections.");
            }
            this.customPipeline = expression.substring(1, expression.length() - 1).trim();
        } else {
            String body = expression.startsWith("{") && expression.endsWith("}") ? expression.substring(1, expression.length() - 1) : expression;
            this.customProjections.addSegment((delimited, dialect) -> body);
        }
    }

    @Override
    public void addSelectAll() {
        this.selectAll = true;
    }

    @Override
    public boolean hasSelect(String col) {
        return selectAll || this.selectedColumns.contains(col);
    }

    @Override
    public boolean hasSelect() {
        return !this.projections.isEmpty() || !this.customProjections.isEmpty() || this.customPipeline != null || this.selectAll;
    }

    @Override
    public void addGroupBy(String col, String colTerm) {
        this.groupedColumns.putIfAbsent(col, StringUtils.isNotBlank(colTerm) ? colTerm : col);
    }

    @Override
    public void addOrderBy(String col, String colTerm, OrderType type, OrderNullsStrategy nullsStrategy) {
        String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
        if (!this.sortedColumns.add(field)) {
            return;
        }
        this.sorts.addSegment((delimited, dialect) -> {
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
        this.insertedColumns.add(col);
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

    private String formatLikeValue(SqlLike likeType, Object value, String valueTerm) {
        String pattern = like(likeType != null ? likeType : SqlLike.DEFAULT, value, null);
        Object parameter = pattern;
        if (value instanceof SqlArg) {
            SqlArg original = (SqlArg) value;
            parameter = new SqlArg(pattern, original.getJdbcType(), original.getTypeHandler());
        }
        return formatValue(parameter, valueTerm);
    }

    private String formatValue(Object value, String valueTerm) {
        this.args.add(value);
        return StringUtils.isNotBlank(valueTerm) ? valueTerm : "?";
    }

    @Override
    public BoundSql buildSelect(boolean delimited) throws SQLException {
        this.args.clear();
        if (!this.groupedColumns.isEmpty() || !this.customProjections.isEmpty() || this.customPipeline != null) {
            return buildAggregate(delimited);
        }
        MergeSqlSegment s = new MergeSqlSegment("");

        s.addSegment((d, dia) -> {
            return dia.tableName(d, catalog, null, collection) + ".find(";
        });

        // Filter
        s.addSegment((d, dia) -> "{");
        s.addSegment((d, dia) -> renderConditions(d));
        s.addSegment((d, dia) -> "}");

        // Projection
        if (!projections.isEmpty() && !selectAll) {
            s.addSegment((d, dia) -> ", {");
            s.addSegment(projections);
            if (!this.selectedColumns.contains("_id")) {
                s.addSegment((d, dia) -> ", _id: 0");
            }
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

    private BoundSql buildAggregate(boolean delimited) throws SQLException {
        List<String> stages = new ArrayList<>();
        if (!this.conditions.isEmpty()) {
            stages.add("{$match: {" + renderConditions(delimited) + "}}");
        }

        if (this.customPipeline != null) {
            if (!this.groupedColumns.isEmpty() || !this.projections.isEmpty()) {
                throw new IllegalArgumentException("Use either groupBy/projections or an explicit MongoDB pipeline.");
            }
            if (!this.customPipeline.isEmpty()) {
                stages.add(this.customPipeline);
            }
        } else if (!this.groupedColumns.isEmpty()) {
            List<String> keys = new ArrayList<>();
            for (Map.Entry<String, String> field : this.groupedColumns.entrySet()) {
                keys.add(field.getKey() + ": '$" + field.getValue() + "'");
            }
            String accumulators = this.customProjections.getSqlSegment(delimited, this);
            stages.add("{$group: {_id: {" + String.join(", ", keys) + "}" + (accumulators.isEmpty() ? "" : ", " + accumulators) + "}}");
            stages.add("{$replaceRoot: {newRoot: {$mergeObjects: ['$_id', '$$ROOT']}}}");
            stages.add("{$project: {_id: 0}}");
        } else {
            String projection = this.customProjections.getSqlSegment(delimited, this);
            String columns = this.projections.getSqlSegment(delimited, this);
            stages.add("{$project: {" + projection + (columns.isEmpty() ? "" : ", " + columns) + "}}");
        }

        if (!this.sorts.isEmpty()) {
            stages.add("{$sort: {" + this.sorts.getSqlSegment(delimited, this) + "}}");
        }
        String command = tableName(delimited, catalog, null, collection) + ".aggregate([" + String.join(", ", stages) + "])";
        return new BoundSql.BoundSqlObj(command, this.args.toArray());
    }

    @Override
    public BoundSql buildUpdate(boolean delimited, boolean allowEmptyWhere) throws SQLException {
        requireMutationCondition(allowEmptyWhere, "UPDATE");
        this.args.clear();
        MergeSqlSegment s = new MergeSqlSegment("");

        s.addSegment((d, dia) -> {
            return dia.tableName(d, catalog, null, collection) + ".updateMany(";
        });

        // Filter
        s.addSegment((d, dia) -> "{");
        s.addSegment((d, dia) -> renderConditions(d));
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
        requireMutationCondition(allowEmptyWhere, "DELETE");
        this.args.clear();
        MergeSqlSegment s = new MergeSqlSegment("");

        s.addSegment((d, dia) -> dia.tableName(d, catalog, null, collection) + ".deleteMany(");

        // Filter
        s.addSegment((d, dia) -> "{");
        s.addSegment((d, dia) -> renderConditions(d));
        s.addSegment((d, dia) -> "}");

        s.addSegment((d, dia) -> ")");

        String sqlString = s.getSqlSegment(delimited, this);
        Object[] sqlArgs = this.args.toArray();
        return new BoundSql.BoundSqlObj(sqlString, sqlArgs);
    }

    @Override
    public BoundSql buildInsert(boolean delimited, List<String> primaryKey, int insertRows,//
            List<String> generatedColumns, DuplicateKeyStrategy duplicateStrategy, GeneratedKeyStrategy generatedStrategy) throws SQLException {
        if (!supportDuplicateStrategy(primaryKey, this.insertedColumns, generatedColumns, duplicateStrategy)) {
            throw new UnsupportedOperationException("MongoDB conflict handling requires every mapped primary key in the inserted document.");
        }
        this.args.clear();
        MergeSqlSegment s = new MergeSqlSegment("");

        s.addSegment((d, dia) -> duplicateHint(primaryKey, duplicateStrategy));

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

    @Override
    public GeneratedKeyStrategy generatedKeyStrategy(List<String> primaryKey, List<String> columns, List<String> returnColumns, DuplicateKeyStrategy strategy) {
        return GeneratedKeyStrategy.OneByOne;
    }

    @Override
    public boolean supportDuplicateStrategy(List<String> primaryKey, List<String> columns, List<String> returnColumns, DuplicateKeyStrategy strategy) {
        if (strategy == null || strategy == DuplicateKeyStrategy.Into) {
            return true;
        }
        return primaryKey != null && !primaryKey.isEmpty() && columns.containsAll(primaryKey);
    }

    private String duplicateHint(List<String> primaryKey, DuplicateKeyStrategy strategy) {
        if (strategy == null || strategy == DuplicateKeyStrategy.Into) {
            return "";
        }
        StringJoiner keys = new StringJoiner(".");
        for (String key : primaryKey) {
            keys.add(Base64.getUrlEncoder().withoutPadding().encodeToString(key.getBytes(StandardCharsets.UTF_8)));
        }
        String mode = strategy == DuplicateKeyStrategy.Ignore ? "ignore" : "update";
        return "/*+ mongo_duplicate_strategy='" + mode + "', mongo_primary_keys='" + keys + "' */";
    }

    @Override
    public String insertSql(DuplicateKeyStrategy strategy, GeneratedKeyStrategy generatedStrategy, boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, List<String> returnColumns, int insertRows, Map<String, String> columnValueTerms) {
        MongoDialect builder = new MongoDialect();
        builder.setTable(catalog, schema, table);
        for (String column : columns) {
            builder.addInsert(column, null, columnValueTerms == null ? null : columnValueTerms.get(column));
        }
        try {
            return builder.buildInsert(useQualifier, primaryKey, insertRows, returnColumns, strategy, generatedStrategy).getSqlString();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to build MongoDB insert command.", e);
        }
    }

    private void requireMutationCondition(boolean allowEmptyWhere, String operation) {
        if (this.conditions.isEmpty() && !allowEmptyWhere) {
            throw new IllegalStateException("The dangerous " + operation + " operation, You must call `allowEmptyWhere()` to enable " + operation + " ALL.");
        }
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        if (isAggregate(boundSql)) {
            return appendAggregateStage(boundSql, "{$facet: {rows: [{$count: 'value'}]}}, {$project: {_id: 0, count: {$ifNull: [{$arrayElemAt: ['$rows.value', 0]}, 0]}}}");
        }
        return new BoundSql.BoundSqlObj("/*+overwrite_find_as_count*/" + boundSql.getSqlString(), boundSql.getArgs());
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        if (isAggregate(boundSql)) {
            return appendAggregateStage(boundSql, (start > 0 ? "{$skip: " + start + "}, " : "") + "{$limit: " + limit + "}");
        }
        StringBuilder sb = new StringBuilder("/*+");

        if (start <= 0) {
            sb.append("overwrite_find_limit=" + limit);
        } else {
            sb.append("overwrite_find_skip=" + start + ",overwrite_find_limit=" + limit);
        }

        sb.append("*/");
        return new BoundSql.BoundSqlObj(sb + boundSql.getSqlString(), boundSql.getArgs());
    }

    private boolean isAggregate(BoundSql boundSql) {
        return boundSql.getSqlString().contains(".aggregate([");
    }

    private BoundSql appendAggregateStage(BoundSql boundSql, String stage) {
        String command = boundSql.getSqlString();
        String prefix = command.substring(0, command.length() - 2);
        return new BoundSql.BoundSqlObj(prefix + (prefix.endsWith("[") ? "" : ", ") + stage + "])", boundSql.getArgs());
    }
}
