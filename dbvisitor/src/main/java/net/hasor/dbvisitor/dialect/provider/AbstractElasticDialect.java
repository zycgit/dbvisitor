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
import java.util.function.Consumer;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;
import net.hasor.dbvisitor.lambda.segment.Segment;
import net.hasor.dbvisitor.types.SqlArg;

/**
 * ES 命令构建器方言
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-06
 */
public abstract class AbstractElasticDialect extends AbstractBuilderDialect implements PageSqlDialect {
    private final   Set<String>          insertColumns     = new LinkedHashSet<>();
    protected       String               index;
    protected       String               type;
    protected final List<Object>         args              = new ArrayList<>();
    private final   List<Segment>        nativeProjections = new ArrayList<>();
    private final   List<String>         groupFields       = new ArrayList<>();
    protected final MergeSqlSegment      conditions        = new MergeSqlSegment(", ");
    protected final MergeSqlSegment      projections       = new MergeSqlSegment(", ");
    protected final MergeSqlSegment      sorts             = new MergeSqlSegment(", ");
    protected final MergeSqlSegment      updates           = new MergeSqlSegment(", ");
    protected final MergeSqlSegment      inserts           = new MergeSqlSegment(", ");
    protected       boolean              selectAll         = false;
    private final   List<ConditionLogic> predicateLogics   = new ArrayList<>();
    private final   List<Segment>        predicates        = new ArrayList<>();

    @Override
    public abstract AbstractElasticDialect newBuilder();

    @Override
    public Set<String> keywords() {
        return Collections.emptySet();
    }

    @Override
    public String tableName(boolean useQualifier, String catalog, String schema, String table) {
        return table;
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

        String strVal = value == null ? "" : value.toString().replace("%", "*").replace("_", "?");
        switch (likeType) {
            case LEFT:
                return "*" + strVal;
            case RIGHT:
                return strVal + "*";
            default:
                return "*" + strVal + "*";
        }
    }

    //

    @Override
    public void setTable(String catalog, String schema, String table) {
        this.index = table;
        if (StringUtils.isBlank(table) && StringUtils.isNotBlank(catalog)) {
            this.index = catalog;
        }
        this.type = schema;
    }

    @Override
    public void clearSelect() {
        this.projections.clear();
        this.nativeProjections.clear();
        this.selectAll = false;
    }

    @Override
    public void clearUpdateSet() {
        this.updates.clear();
    }

    @Override
    public void clearAll() {
        this.index = null;
        this.type = null;
        this.args.clear();
        this.conditions.clear();
        this.predicateLogics.clear();
        this.predicates.clear();
        this.projections.clear();
        this.nativeProjections.clear();
        this.groupFields.clear();
        this.sorts.clear();
        this.updates.clear();
        this.inserts.clear();
        this.insertColumns.clear();
        this.selectAll = false;
    }

    @Override
    public void addCondition(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value, String valueTerm, SqlLike forLikeType) {
        addPredicate(logic, (delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            if (type == ConditionType.LIKE || type == ConditionType.NOT_LIKE) {
                Object rawValue = value instanceof SqlArg ? ((SqlArg) value).getValue() : value;
                Object pattern = dialect.like(forLikeType != null ? forLikeType : SqlLike.DEFAULT, rawValue, valueTerm);
                if (value instanceof SqlArg) {
                    SqlArg source = (SqlArg) value;
                    pattern = new SqlArg(source.getName(), pattern, source.getSqlMode(), source.getJdbcType(), source.getJavaType(), source.getTypeHandler());
                }
                String val = formatValue(pattern, valueTerm);
                String wildcard = "{ \"wildcard\": { \"" + field + "\": " + val + " } }";
                return type == ConditionType.LIKE ? wildcard : "{ \"bool\": { \"must_not\": " + wildcard + " } }";
            }

            if (type == ConditionType.IS_NULL) {
                return "{ \"bool\": { \"must_not\": { \"exists\": { \"field\": \"" + field + "\" } } } }";
            }
            if (type == ConditionType.IS_NOT_NULL) {
                return "{ \"exists\": { \"field\": \"" + field + "\" } }";
            }
            String val = formatValue(value, valueTerm);

            if (type == ConditionType.EQ) {
                return "{ \"match\": { \"" + field + "\": " + val + " } }";
            }
            if (type == ConditionType.NE) {
                return "{ \"bool\": { \"must_not\": { \"term\": { \"" + field + "\": " + val + " } } } }";
            }
            if (type == ConditionType.GT) {
                return "{ \"range\": { \"" + field + "\": { \"gt\": " + val + " } } }";
            }
            if (type == ConditionType.GE) {
                return "{ \"range\": { \"" + field + "\": { \"gte\": " + val + " } } }";
            }
            if (type == ConditionType.LT) {
                return "{ \"range\": { \"" + field + "\": { \"lt\": " + val + " } } }";
            }
            if (type == ConditionType.LE) {
                return "{ \"range\": { \"" + field + "\": { \"lte\": " + val + " } } }";
            }
            throw new UnsupportedOperationException("Unsupported condition type: " + type);
        });
    }

    @Override
    public void addConditionForBetween(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value1, String value1Term, Object value2, String value2Term) {
        addPredicate(logic, (delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            String val1 = formatValue(value1, value1Term);
            String val2 = formatValue(value2, value2Term);

            if (type == ConditionType.BETWEEN) {
                return "{ \"range\": { \"" + field + "\": { \"gte\": " + val1 + ", \"lte\": " + val2 + " } } }";
            }
            if (type == ConditionType.NOT_BETWEEN) {
                return "{ \"bool\": { \"must_not\": { \"range\": { \"" + field + "\": { \"gte\": " + val1 + ", \"lte\": " + val2 + " } } } } }";
            }
            throw new UnsupportedOperationException("Unsupported condition type: " + type);
        });
    }

    @Override
    public void addConditionForIn(ConditionLogic logic, String col, String colTerm, ConditionType type, Object[] values, String valueTerm) {
        addPredicate(logic, (delimited, dialect) -> {
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
            String val = sb.toString();

            if (type == ConditionType.IN) {
                return "{ \"terms\": { \"" + field + "\": " + val + " } }";
            }
            if (type == ConditionType.NOT_IN) {
                return "{ \"bool\": { \"must_not\": { \"terms\": { \"" + field + "\": " + val + " } } } }";
            }
            throw new UnsupportedOperationException("Unsupported condition type: " + type);
        });
    }

    @Override
    public void addRawCondition(ConditionLogic logic, BoundSql boundSql) {
        addPredicate(logic, (delimited, dialect) -> {
            Collections.addAll(this.args, boundSql.getArgs());
            return boundSql.getSqlString();
        });
    }

    @Override
    public void addConditionGroup(ConditionLogic logic, Consumer<SqlCommandBuilder> group) {
        AbstractElasticDialect subBuilder = this.newBuilder();
        group.accept(subBuilder);
        addPredicate(logic, (delimited, dialect) -> {
            subBuilder.args.clear();
            String subSql = subBuilder.conditions.getSqlSegment(delimited, dialect);
            this.args.addAll(subBuilder.args);
            if (StringUtils.isBlank(subSql)) {
                return "";
            }
            return "{ \"bool\": { \"must\": [" + subSql + "] } }";
        });
    }

    protected void addPredicate(ConditionLogic logic, Segment predicate) {
        if (predicates.isEmpty()) {
            conditions.addSegment((delimited, dialect) -> renderPredicates(delimited));
        }
        predicateLogics.add(logic);
        predicates.add(predicate);
    }

    private String renderPredicates(boolean useQualifier) throws SQLException {
        List<String> alternatives = new ArrayList<>();
        List<String> conjunction = new ArrayList<>();
        for (int i = 0; i < predicates.size(); i++) {
            String query = predicates.get(i).getSqlSegment(useQualifier, this);
            if (StringUtils.isBlank(query)) {
                continue;
            }
            ConditionLogic logic = predicateLogics.get(i);
            if (logic == ConditionLogic.OR || logic == ConditionLogic.OR_NOT) {
                if (!conjunction.isEmpty()) {
                    alternatives.add(joinMust(conjunction));
                    conjunction.clear();
                }
            }
            if (logic == ConditionLogic.AND_NOT || logic == ConditionLogic.OR_NOT) {
                query = "{\"bool\": {\"must_not\": " + query + "}}";
            }
            conjunction.add(query);
        }
        if (!conjunction.isEmpty()) {
            alternatives.add(joinMust(conjunction));
        }
        if (alternatives.isEmpty()) {
            return "{\"match_all\": {}}";
        }
        return alternatives.size() == 1 ? alternatives.get(0) : "{\"bool\": {\"should\": [" + String.join(",", alternatives) + "],\"minimum_should_match\": 1}}";
    }

    private String joinMust(List<String> queries) {
        return queries.size() == 1 ? queries.get(0) : "{\"bool\": {\"must\": [" + String.join(",", queries) + "]}}";
    }

    @Override
    public void addSelect(String col, String colTerm) {
        this.projections.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            return "\"" + field + "\"";
        });
    }

    @Override
    public void addSelectCustom(String custom, Object[] args) {
        String expression = custom.trim();
        if (expression.startsWith("{") && expression.endsWith("}")) {
            this.nativeProjections.add((delimited, dialect) -> {
                if (args != null) {
                    Collections.addAll(this.args, args);
                }
                return expression.substring(1, expression.length() - 1);
            });
            return;
        }
        this.projections.addSegment((delimited, dialect) -> {
            return "\"" + custom + "\"";
        });
    }

    @Override
    public void addSelectAll() {
        this.selectAll = true;
    }

    @Override
    public boolean hasSelect(String col) {
        return false;
    }

    @Override
    public boolean hasSelect() {
        return !this.projections.isEmpty() || !this.nativeProjections.isEmpty() || this.selectAll;
    }

    @Override
    public void addGroupBy(String col, String colTerm) {
        String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
        if (field == null || !field.matches("[A-Za-z_][A-Za-z0-9_.]*")) {
            throw new IllegalArgumentException("Elasticsearch groupBy requires a mapped field name");
        }
        if (!this.groupFields.contains(field)) {
            this.groupFields.add(field);
        }
    }

    @Override
    public void addOrderBy(String col, String colTerm, OrderType type, OrderNullsStrategy nullsStrategy) {
        this.sorts.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            String order = (type == OrderType.DESC) ? "desc" : "asc";
            String missing = nullsStrategy == OrderNullsStrategy.FIRST ? ", \"missing\": \"_first\"" : nullsStrategy == OrderNullsStrategy.LAST ? ", \"missing\": \"_last\"" : "";
            return "{ \"" + field + "\": { \"order\": \"" + order + "\"" + missing + " } }";
        });
    }

    @Override
    public void addUpdateSet(String col, Object value, String valueTerm) {
        this.updates.addSegment((delimited, dialect) -> {
            String val = formatValue(value, valueTerm);
            return "\"" + col + "\": " + val;
        });
    }

    @Override
    public void addInsert(String col, Object value, String valueTerm) {
        this.insertColumns.add(col);
        this.inserts.addSegment((delimited, dialect) -> {
            String val = formatValue(value, valueTerm);
            return "\"" + col + "\": " + val;
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

    protected String formatValue(Object value, String valueTerm) {
        if (StringUtils.isNotBlank(valueTerm)) {
            return valueTerm;
        }
        this.args.add(value);
        return "?";
    }

    @Override
    public BoundSql buildSelect(boolean useQualifier) throws SQLException {
        this.args.clear();
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Query
        json.append("\"query\": ").append(renderSelectQuery(useQualifier)).append(", ");

        // Source (Projections)
        if (!this.projections.isEmpty() && !this.selectAll && this.nativeProjections.isEmpty()) {
            json.append("\"_source\": [");
            json.append(this.projections.getSqlSegment(useQualifier, this));
            json.append("], ");
        }

        for (Segment projection : this.nativeProjections) {
            String fragment = projection.getSqlSegment(useQualifier, this);
            if (StringUtils.isNotBlank(fragment)) {
                json.append(fragment).append(", ");
            }
        }

        // Sort
        if (!this.sorts.isEmpty()) {
            json.append("\"sort\": [");
            json.append(this.sorts.getSqlSegment(useQualifier, this));
            json.append("]");
        } else {
            // Remove trailing comma if exists
            if (json.length() > 2 && json.charAt(json.length() - 2) == ',') {
                json.setLength(json.length() - 2);
            }
        }

        json.append("}");

        String method = "POST";
        String endpoint = getSearchEndpoint();
        String hint = this.groupFields.isEmpty() ? "" : "/*+ aggregation_group=\"" + String.join(",", this.groupFields) + "\" */ ";
        String command = hint + method + " " + endpoint + " " + json;

        return new BoundSql.BoundSqlObj(command, this.args.toArray());
    }

    protected String renderSelectQuery(boolean useQualifier) throws SQLException {
        return this.conditions.isEmpty() ? "{\"match_all\": {}}" : "{\"bool\": {\"must\": [" + this.conditions.getSqlSegment(useQualifier, this) + "]}}";
    }

    protected abstract String getSearchEndpoint();

    protected abstract String getInsertEndpoint();

    protected abstract String getUpdateEndpoint();

    protected abstract String getDeleteEndpoint();

    @Override
    public BoundSql buildInsert(boolean useQualifier, List<String> primaryKey, int insertRows, List<String> generatedColumns, DuplicateKeyStrategy duplicateStrategy, GeneratedKeyStrategy generatedStrategy) throws SQLException {
        this.args.clear();
        String endpoint = getInsertEndpoint();
        int keyIndex = -1;
        if (primaryKey != null && primaryKey.size() == 1) {
            keyIndex = new ArrayList<>(this.insertColumns).indexOf(primaryKey.get(0));
        }
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append(this.inserts.getSqlSegment(useQualifier, this));
        json.append("}");

        String method = "POST";
        DuplicateKeyStrategy strategy = duplicateStrategy == null ? DuplicateKeyStrategy.Into : duplicateStrategy;
        String hint = keyIndex < 0 ? "" : "/*+ document_id_column=" + (keyIndex + 1) + ", duplicate_strategy=" + strategy.name() + " */ ";
        String command = hint + method + " " + endpoint + " " + json.toString();

        return new BoundSql.BoundSqlObj(command, this.args.toArray());
    }

    @Override
    public BoundSql buildUpdate(boolean useQualifier, boolean allowEmptyWhere) throws SQLException {
        requireMutationCondition(allowEmptyWhere, "UPDATE");
        this.args.clear();
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Query
        if (!this.conditions.isEmpty()) {
            json.append("\"query\": { \"bool\": { \"must\": [");
            json.append(this.conditions.getSqlSegment(useQualifier, this));
            json.append("] } }, ");
        }

        // Script
        json.append("\"script\": { \"source\": \"ctx._source.putAll(params.data)\", \"lang\": \"painless\", \"params\": { \"data\": {");
        json.append(this.updates.getSqlSegment(useQualifier, this));
        json.append("} } }");

        json.append("}"); // End of body

        String method = "POST";
        String endpoint = getUpdateEndpoint(); // _update_by_query
        String command = method + " " + endpoint + " " + json.toString();

        return new BoundSql.BoundSqlObj(command, this.args.toArray());
    }

    @Override
    public BoundSql buildDelete(boolean useQualifier, boolean allowEmptyWhere) throws SQLException {
        requireMutationCondition(allowEmptyWhere, "DELETE");
        this.args.clear();
        StringBuilder json = new StringBuilder();
        json.append("{");

        if (!this.conditions.isEmpty()) {
            json.append("\"query\": { \"bool\": { \"must\": [");
            json.append(this.conditions.getSqlSegment(useQualifier, this));
            json.append("] } }");
        } else {
            json.append("\"query\": { \"match_all\": {} }");
        }

        json.append("}");

        String method = "POST";
        String endpoint = getDeleteEndpoint(); // _delete_by_query
        String command = method + " " + endpoint + " " + json.toString();

        return new BoundSql.BoundSqlObj(command, this.args.toArray());
    }

    private void requireMutationCondition(boolean allowEmptyWhere, String operation) {
        if (this.conditions.isEmpty() && !allowEmptyWhere) {
            throw new IllegalStateException("The dangerous " + operation + " operation, You must call `allowEmptyWhere()` to enable " + operation + " ALL.");
        }
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        return new BoundSql.BoundSqlObj("/*+overwrite_find_as_count*/" + boundSql.getSqlString(), boundSql.getArgs());
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sqlBuilder = new StringBuilder("/*+");

        if (start <= 0) {
            sqlBuilder.append("overwrite_find_limit=" + limit);
        } else {
            sqlBuilder.append("overwrite_find_skip=" + start + ",overwrite_find_limit=" + limit);
        }

        sqlBuilder.append("*/");
        return new BoundSql.BoundSqlObj(sqlBuilder + boundSql.getSqlString(), boundSql.getArgs());
    }
}
