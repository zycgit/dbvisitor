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
package net.hasor.dbvisitor.lambda.core;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.hasor.cobble.ObjectUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.Tuple;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.dbvisitor.dialect.builder.CommandBuilder;
import net.hasor.dbvisitor.dialect.builder.ConditionLogic;
import net.hasor.dbvisitor.dialect.builder.ConditionType;
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.args.ArraySqlArgSource;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * 扩展了 AbstractQueryExecute 提供 lambda 方式生成 SQL。 实现了 Compare 接口。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-27
 */
public abstract class BasicQueryCompare<R, T, P> extends BasicLambda<R, T, P> implements QueryCompare<R, T, P> {
    private ConditionLogic nextLogic = ConditionLogic.AND;

    public BasicQueryCompare(Class<?> exampleType, TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(exampleType, tableMapping, registry, jdbc, ctx);
    }

    @Override
    public R reset() {
        super.reset();
        this.nextLogic = ConditionLogic.AND;
        return this.getSelf();
    }

    @Override
    public R ifTrue(boolean test, Consumer<QueryCompare<R, T, P>> lambda) {
        if (test) {
            lambda.accept(this);
        }
        return getSelf();
    }

    @Override
    public R nested(Consumer<R> lambda) {
        ConditionLogic tempLogic = this.nextLogic;
        this.nextLogic = ConditionLogic.AND;
        this.cmdBuilder.addConditionGroup(tempLogic, cb -> {
            CommandBuilder oldBuilder = this.cmdBuilder;
            this.cmdBuilder = cb;
            try {
                lambda.accept(this.getSelf());
            } finally {
                this.cmdBuilder = oldBuilder;
            }
        });
        return this.getSelf();
    }

    @Override
    public R nested(boolean test, Consumer<R> lambda) {
        return test ? nested(lambda) : getSelf();
    }

    @Override
    public R or() {
        this.nextLogic = ConditionLogic.OR;
        return this.getSelf();
    }

    @Override
    public R or(boolean test, Consumer<R> lambda) {
        return test ? or(lambda) : getSelf();
    }

    @Override
    public R and() {
        this.nextLogic = ConditionLogic.AND;
        return this.getSelf();
    }

    @Override
    public R and(boolean test, Consumer<R> lambda) {
        return test ? and(lambda) : getSelf();
    }

    @Override
    public R not() {
        if (this.nextLogic == ConditionLogic.OR) {
            this.nextLogic = ConditionLogic.OR_NOT;
        } else {
            this.nextLogic = ConditionLogic.AND_NOT;
        }
        return this.getSelf();
    }

    @Override
    public R not(boolean test, Consumer<R> lambda) {
        if (test) {
            this.not();
            this.nested(lambda);
        }
        return this.getSelf();
    }

    @Override
    public R apply(final String sqlString, final Object... args) throws SQLException {
        if (StringUtils.isBlank(sqlString)) {
            return this.getSelf();
        }

        PlanDynamicSql parsedSql = DynamicParsed.getParsedSql(sqlString);
        SqlBuilder build = parsedSql.buildQuery(new ArraySqlArgSource(args), this.queryContext);
        this.cmdBuilder.addRawCondition(this.nextLogic, build);
        this.nextLogic = ConditionLogic.AND;
        return this.getSelf();
    }

    @Override
    public R eq(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            if (value == null) {
                this.addCondition(propertyName, ConditionType.IS_NULL, null);
            } else {
                this.addCondition(propertyName, ConditionType.EQ, value);
            }
        }
        return this.getSelf();
    }

    @Override
    public R ne(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            if (value == null) {
                this.addCondition(propertyName, ConditionType.IS_NOT_NULL, null);
            } else {
                this.addCondition(propertyName, ConditionType.NE, value);
            }
        }
        return this.getSelf();
    }

    @Override
    public R gt(boolean test, P property, Object value) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.GT, value);
        }
        return this.getSelf();
    }

    @Override
    public R ge(boolean test, P property, Object value) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.GE, value);
        }
        return this.getSelf();
    }

    @Override
    public R lt(boolean test, P property, Object value) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.LT, value);
        }
        return this.getSelf();
    }

    @Override
    public R le(boolean test, P property, Object value) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.LE, value);
        }
        return this.getSelf();
    }

    @Override
    public R like(boolean test, P property, Object value) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.LIKE, value, SqlLike.DEFAULT);
        }
        return this.getSelf();
    }

    @Override
    public R notLike(boolean test, P property, Object value) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.NOT_LIKE, value, SqlLike.DEFAULT);
        }
        return this.getSelf();
    }

    @Override
    public R likeRight(boolean test, P property, Object value) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.LIKE, value, SqlLike.RIGHT);
        }
        return this.getSelf();
    }

    @Override
    public R notLikeRight(boolean test, P property, Object value) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.NOT_LIKE, value, SqlLike.RIGHT);
        }
        return this.getSelf();
    }

    @Override
    public R likeLeft(boolean test, P property, Object value) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.LIKE, value, SqlLike.LEFT);
        }
        return this.getSelf();
    }

    @Override
    public R notLikeLeft(boolean test, P property, Object value) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.NOT_LIKE, value, SqlLike.LEFT);
        }
        return this.getSelf();
    }

    @Override
    public R isNull(boolean test, P property) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.IS_NULL, null);
        }
        return this.getSelf();
    }

    @Override
    public R isNotNull(boolean test, P property) {
        if (test) {
            this.addCondition(getPropertyName(property), ConditionType.IS_NOT_NULL, null);
        }
        return this.getSelf();
    }

    @Override
    public R in(boolean test, P property, Collection<?> value) {
        if (test) {
            ObjectUtils.assertTrue(!value.isEmpty(), "build notIn failed, value is empty.");
            this.addConditionForIn(getPropertyName(property), ConditionType.IN, value);
        }
        return this.getSelf();
    }

    @Override
    public R notIn(boolean test, P property, Collection<?> value) {
        if (test) {
            ObjectUtils.assertTrue(!value.isEmpty(), "build notIn failed, value is empty.");
            this.addConditionForIn(getPropertyName(property), ConditionType.NOT_IN, value);
        }
        return this.getSelf();
    }

    @Override
    public R rangeBetween(boolean test, P property, Object value1, Object value2) {
        if (test) {
            this.addConditionForBetween(getPropertyName(property), ConditionType.BETWEEN, value1, value2);
        }
        return this.getSelf();
    }

    @Override
    public R rangeNotBetween(boolean test, P property, Object value1, Object value2) {
        if (test) {
            this.addConditionForBetween(getPropertyName(property), ConditionType.NOT_BETWEEN, value1, value2);
        }
        return this.getSelf();
    }

    @Override
    public R rangeOpenOpen(boolean test, P property, Object value1, Object value2) {
        return test ? nested(p -> {
            this.gt(property, value1);
            this.lt(property, value2);
        }) : getSelf();
    }

    @Override
    public R rangeNotOpenOpen(boolean test, P property, Object value1, Object value2) {
        return test ? not(p -> {
            this.gt(property, value1);
            this.lt(property, value2);
        }) : getSelf();
    }

    @Override
    public R rangeOpenClosed(boolean test, P property, Object value1, Object value2) {
        return test ? nested(p -> {
            this.gt(property, value1);
            this.le(property, value2);
        }) : getSelf();
    }

    @Override
    public R rangeNotOpenClosed(boolean test, P property, Object value1, Object value2) {
        return test ? not(p -> {
            this.gt(property, value1);
            this.le(property, value2);
        }) : getSelf();
    }

    @Override
    public R rangeClosedOpen(boolean test, P property, Object value1, Object value2) {
        return test ? nested(p -> {
            this.ge(property, value1);
            this.lt(property, value2);
        }) : getSelf();
    }

    @Override
    public R rangeNotClosedOpen(boolean test, P property, Object value1, Object value2) {
        return test ? not(p -> {
            this.ge(property, value1);
            this.lt(property, value2);
        }) : getSelf();
    }

    @Override
    public R rangeClosedClosed(boolean test, P property, Object value1, Object value2) {
        return test ? nested(p -> {
            this.ge(property, value1);
            this.le(property, value2);
        }) : getSelf();
    }

    @Override
    public R rangeNotClosedClosed(boolean test, P property, Object value1, Object value2) {
        return test ? not(p -> {
            this.ge(property, value1);
            this.le(property, value2);
        }) : getSelf();
    }

    @Override
    public R eqBySample(T sample) {
        if (sample == null) {
            throw new NullPointerException("sample is null.");
        }

        if (exampleIsMap()) {
            return this.eqBySampleMap((Map<String, Object>) sample);
        }

        final List<Tuple> condition = new ArrayList<>();
        for (ColumnMapping property : this.getTableMapping().getProperties()) {
            if (property.getHandler().get(sample) != null) {
                Object value = property.getHandler().get(sample);
                if (value != null) {
                    condition.add(Tuple.of(property.getProperty(), value));
                }
            }
        }

        if (!condition.isEmpty()) {
            this.nested(p -> {
                for (Tuple tuple : condition) {
                    this.addCondition(tuple.getArg0(), ConditionType.EQ, tuple.getArg1());
                }
            });
        }

        return this.getSelf();
    }

    @Override
    public R eqBySampleMap(Map<String, Object> sample) {
        if (sample == null) {
            throw new NullPointerException("sample is null.");
        }

        TableMapping<?> tableMapping = this.getTableMapping();
        final List<Tuple> condition = new ArrayList<>();
        if (!tableMapping.getProperties().isEmpty()) {
            // use column def.
            for (ColumnMapping property : tableMapping.getProperties()) {
                String propertyName = property.getProperty();
                Object value = sample.get(propertyName);
                if (value != null) {
                    condition.add(Tuple.of(propertyName, value));
                }
            }
        } else {
            // not found any column.
            for (String propertyName : sample.keySet()) {
                Object value = sample.get(propertyName);
                if (value != null) {
                    condition.add(Tuple.of(propertyName, value));
                }
            }
        }

        if (!condition.isEmpty()) {
            this.nested(p -> {
                for (Tuple tuple : condition) {
                    this.addCondition(tuple.getArg0(), ConditionType.EQ, tuple.getArg1());
                }
            });
        }

        return this.getSelf();
    }

    protected SqlArg wrapValue(String propertyName, Object value) {
        if (value instanceof SqlArg) {
            return (SqlArg) value;
        }
        ColumnMapping mapping = this.findPropertyByName(propertyName);
        int sqlType = Types.OTHER;
        TypeHandler<?> typeHandler = null;

        if (mapping != null) {
            Integer jdbcType = mapping.getJdbcType();
            if (jdbcType != null) {
                sqlType = jdbcType;
            }
            if (!exampleIsMap()) {
                typeHandler = mapping.getTypeHandler();
            }
        } else if (value != null) {
            sqlType = TypeHandlerRegistry.toSqlType(value.getClass());
            typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(value.getClass());
        }

        return new SqlArg(value, sqlType, typeHandler);
    }

    protected SqlArg[] wrapValues(String propertyName, Object[] values) {
        if (values == null) {
            return new SqlArg[0];
        }
        SqlArg[] args = new SqlArg[values.length];
        for (int i = 0; i < values.length; i++) {
            args[i] = wrapValue(propertyName, values[i]);
        }
        return args;
    }

    protected void addCondition(String propertyName, ConditionType type, Object value) {
        addCondition(propertyName, type, value, null);
    }

    protected void addCondition(String propertyName, ConditionType type, Object value, SqlLike likeType) {
        ColumnMapping mapping = this.findPropertyByName(propertyName);
        String colName = mapping != null ? mapping.getColumn() : propertyName;
        String colTerm = mapping != null ? mapping.getWhereColTemplate() : null;
        String valTerm = mapping != null ? mapping.getWhereValueTemplate() : null;

        Object val = wrapValue(propertyName, value);

        this.cmdBuilder.addCondition(this.nextLogic, colName, colTerm, type, val, valTerm, likeType);
        this.nextLogic = ConditionLogic.AND;
    }

    protected void addConditionForIn(String propertyName, ConditionType type, Collection<?> value) {
        ColumnMapping mapping = this.findPropertyByName(propertyName);
        String colName = mapping != null ? mapping.getColumn() : propertyName;
        String colTerm = mapping != null ? mapping.getWhereColTemplate() : null;
        String valTerm = mapping != null ? mapping.getWhereValueTemplate() : null;

        Object[] values = wrapValues(propertyName, value.toArray());

        this.cmdBuilder.addConditionForIn(this.nextLogic, colName, colTerm, type, values, valTerm);
        this.nextLogic = ConditionLogic.AND;
    }

    protected void addConditionForBetween(String propertyName, ConditionType type, Object value1, Object value2) {
        ColumnMapping mapping = this.findPropertyByName(propertyName);
        String colName = mapping != null ? mapping.getColumn() : propertyName;
        String colTerm = mapping != null ? mapping.getWhereColTemplate() : null;
        String valTerm = mapping != null ? mapping.getWhereValueTemplate() : null;

        Object val1 = wrapValue(propertyName, value1);
        Object val2 = wrapValue(propertyName, value2);

        this.cmdBuilder.addConditionForBetween(this.nextLogic, colName, colTerm, type, val1, valTerm, val2, valTerm);
        this.nextLogic = ConditionLogic.AND;
    }
}