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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;

/**
 * ES 命令构建器方言
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-06
 */
public abstract class AbstractElasticDialect extends AbstractBuilderDialect implements PageSqlDialect {
    protected       String          index;
    protected       String          type;
    protected final List<Object>    args        = new ArrayList<>();
    protected final MergeSqlSegment conditions  = new MergeSqlSegment(", ");
    protected final MergeSqlSegment projections = new MergeSqlSegment(", ");
    protected final MergeSqlSegment sorts       = new MergeSqlSegment(", ");
    protected final MergeSqlSegment updates     = new MergeSqlSegment(", ");
    protected final MergeSqlSegment inserts     = new MergeSqlSegment(", ");
    protected       boolean         selectAll   = false;

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

        String strVal = value == null ? "" : value.toString();
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
            String val = formatValue(value, valueTerm);

            if (type == ConditionType.LIKE) {
                val = dialect.like(forLikeType != null ? forLikeType : SqlLike.DEFAULT, value, valueTerm);
                return "{ \"wildcard\": { \"" + field + "\": \"" + val + "\" } }";
            }
            if (type == ConditionType.NOT_LIKE) {
                val = dialect.like(forLikeType != null ? forLikeType : SqlLike.DEFAULT, value, valueTerm);
                return "{ \"bool\": { \"must_not\": { \"wildcard\": { \"" + field + "\": \"" + val + "\" } } } }";
            }

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
            if (type == ConditionType.IS_NULL) {
                return "{ \"bool\": { \"must_not\": { \"exists\": { \"field\": \"" + field + "\" } } } }";
            }
            if (type == ConditionType.IS_NOT_NULL) {
                return "{ \"exists\": { \"field\": \"" + field + "\" } }";
            }
            throw new UnsupportedOperationException("Unsupported condition type: " + type);
        });
    }

    @Override
    public void addConditionForBetween(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value1, String value1Term, Object value2, String value2Term) {
        this.conditions.addSegment((delimited, dialect) -> {
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
        this.conditions.addSegment((delimited, dialect) -> {
            return boundSql.getSqlString();
        });
        Collections.addAll(this.args, boundSql.getArgs());
    }

    @Override
    public void addConditionGroup(ConditionLogic logic, Consumer<SqlCommandBuilder> group) {
        AbstractElasticDialect subBuilder = this.newBuilder();
        group.accept(subBuilder);
        this.conditions.addSegment((delimited, dialect) -> {
            String subSql = subBuilder.conditions.getSqlSegment(delimited, dialect);
            if (StringUtils.isBlank(subSql)) {
                return "";
            }
            if (logic == ConditionLogic.OR) {
                return "{ \"bool\": { \"should\": [" + subSql + "] } }";
            } else {
                return "{ \"bool\": { \"must\": [" + subSql + "] } }";
            }
        });
        this.args.addAll(subBuilder.args);
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
        return !this.projections.isEmpty() || this.selectAll;
    }

    @Override
    public void addGroupBy(String col, String colTerm) {
        throw new UnsupportedOperationException("ES does not support GroupBy in this builder yet.");
    }

    @Override
    public void addOrderBy(String col, String colTerm, OrderType type, OrderNullsStrategy nullsStrategy) {
        this.sorts.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            String order = (type == OrderType.ASC) ? "asc" : "desc";
            return "{ \"" + field + "\": { \"order\": \"" + order + "\" } }";
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
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Query
        if (!this.conditions.isEmpty()) {
            json.append("\"query\": { \"bool\": { \"must\": [");
            json.append(this.conditions.getSqlSegment(useQualifier, this));
            json.append("] } }, ");
        } else {
            json.append("\"query\": { \"match_all\": {} }, ");
        }

        // Source (Projections)
        if (this.hasSelect() && !this.selectAll) {
            json.append("\"_source\": [");
            json.append(this.projections.getSqlSegment(useQualifier, this));
            json.append("], ");
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
        String command = method + " " + endpoint + " " + json;

        return new BoundSql.BoundSqlObj(command, this.args.toArray());
    }

    protected abstract String getSearchEndpoint();

    protected abstract String getInsertEndpoint();

    protected abstract String getUpdateEndpoint();

    protected abstract String getDeleteEndpoint();

    @Override
    public BoundSql buildInsert(boolean useQualifier, List<String> primaryKey, DuplicateKeyStrategy duplicateKeyStrategy) throws SQLException {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append(this.inserts.getSqlSegment(useQualifier, this));
        json.append("}");

        String method = "POST";
        String endpoint = getInsertEndpoint();
        String command = method + " " + endpoint + " " + json.toString();

        return new BoundSql.BoundSqlObj(command, this.args.toArray());
    }

    @Override
    public BoundSql buildUpdate(boolean useQualifier, boolean useReplace) throws SQLException {
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
    public BoundSql buildDelete(boolean useQualifier, boolean useReplace) throws SQLException {
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
