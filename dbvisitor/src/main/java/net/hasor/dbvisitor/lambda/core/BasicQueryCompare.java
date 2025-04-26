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
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.args.ArraySqlArgSource;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;
import net.hasor.dbvisitor.lambda.segment.Segment;
import net.hasor.dbvisitor.lambda.segment.SqlKeyword;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.Types;
import java.util.*;
import java.util.function.Consumer;

/**
 * 扩展了 AbstractQueryExecute 提供 lambda 方式生成 SQL。 实现了 Compare 接口。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-27
 */
public abstract class BasicQueryCompare<R, T, P> extends BasicLambda<R, T, P> implements QueryCompare<R, T, P> {
    protected MergeSqlSegment queryTemplate     = new MergeSqlSegment();
    protected List<Object>    queryParam        = new ArrayList<>();
    private   Segment         nextSegmentPrefix = null;
    private   boolean         lockCondition     = false;

    public BasicQueryCompare(Class<?> exampleType, TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(exampleType, tableMapping, registry, jdbc, ctx);
    }

    @Override
    public R reset() {
        super.reset();
        this.queryTemplate.cleanSegment();
        this.queryParam.clear();
        this.nextSegmentPrefix = null;
        this.lockCondition = false;
        return this.getSelf();
    }

    @Override
    public R ifTrue(boolean test, Consumer<QueryCompare<R, T, P>> lambda) {
        if (test) {
            lambda.accept(this);
            return getSelf();
        }
        return getSelf();
    }

    @Override
    public R nested(Consumer<R> lambda) {
        this.addCondition(SqlKeyword.LEFT);
        this.nextSegmentPrefix = SqlKeyword.EMPTY;
        lambda.accept(this.getSelf());
        this.nextSegmentPrefix = SqlKeyword.EMPTY;
        this.addCondition(SqlKeyword.RIGHT);
        return this.getSelf();
    }

    @Override
    public R nested(boolean test, Consumer<R> lambda) {
        return test ? nested(lambda) : getSelf();
    }

    @Override
    public R or() {
        this.nextSegmentPrefix = SqlKeyword.OR;
        return this.getSelf();
    }

    @Override
    public R or(boolean test, Consumer<R> lambda) {
        return test ? or(lambda) : getSelf();
    }

    @Override
    public R and() {
        this.nextSegmentPrefix = SqlKeyword.AND;
        return this.getSelf();
    }

    @Override
    public R and(boolean test, Consumer<R> lambda) {
        return test ? and(lambda) : getSelf();
    }

    @Override
    public R not() {
        this.nextSegmentPrefix = SqlKeyword.NOT;
        return this.getSelf();
    }

    @Override
    public R not(boolean test, Consumer<R> lambda) {
        return test ? and(lambda) : getSelf();
    }

    @Override
    public R apply(final String sqlString, final Object... args) {
        if (StringUtils.isBlank(sqlString)) {
            return this.getSelf();
        }
        this.queryTemplate.addSegment(d -> {
            PlanDynamicSql parsedSql = DynamicParsed.getParsedSql(sqlString);
            SqlBuilder build = parsedSql.buildQuery(new ArraySqlArgSource(args), this.queryContext);
            for (Object arg : build.getArgs()) {
                format("?", arg);
            }
            return build.getSqlString();
        });
        return this.getSelf();
    }

    protected void lockCondition() {
        this.lockCondition = true;
    }

    protected final R addCondition(Segment... segments) {
        if (this.lockCondition) {
            throw new IllegalStateException("must before (group by/order by) invoke it.");
        }

        if (this.nextSegmentPrefix == SqlKeyword.EMPTY) {
            this.nextSegmentPrefix = null;
        } else if (this.nextSegmentPrefix == null) {
            this.queryTemplate.addSegment(SqlKeyword.AND);
            this.nextSegmentPrefix = null;
        } else {
            this.queryTemplate.addSegment(this.nextSegmentPrefix);
            this.nextSegmentPrefix = null;
        }

        for (Segment segment : segments) {
            this.queryTemplate.addSegment(segment);
        }
        return this.getSelf();
    }

    protected Segment formatLikeValue(String property, SqlLike like, Object param) {
        ColumnMapping mapping = this.findPropertyByName(property);
        String specialValue = mapping.getWhereValueTemplate();
        String colValue = StringUtils.isNotBlank(specialValue) ? specialValue : "?";

        return d -> {
            format(colValue, param);
            return ((ConditionSqlDialect) d).like(like, param);
        };
    }

    protected Segment formatValue(String property, Object... params) {
        if (ArrayUtils.isEmpty(params)) {
            return d -> "";
        }

        ColumnMapping mapping = this.findPropertyByName(property);
        String specialValue = mapping.getWhereValueTemplate();
        String colValue = StringUtils.isNotBlank(specialValue) ? specialValue : "?";

        MergeSqlSegment mergeSqlSegment = new MergeSqlSegment();
        Iterator<Object> iterator = Arrays.asList(params).iterator();
        while (iterator.hasNext()) {
            Object arg = iterator.next();
            SqlArg sqlArg;
            if (arg instanceof SqlArg) {
                sqlArg = (SqlArg) arg;
            } else {
                sqlArg = new SqlArg(arg, mapping.getJdbcType(), exampleIsMap() ? null : mapping.getTypeHandler());
            }
            mergeSqlSegment.addSegment(formatSegment(colValue, sqlArg));
            if (iterator.hasNext()) {
                mergeSqlSegment.addSegment(d -> ",");
            }
        }
        return mergeSqlSegment;
    }

    protected Segment formatValue(Object... params) {
        if (ArrayUtils.isEmpty(params)) {
            return d -> "";
        }

        String colValue = "?";
        MergeSqlSegment mergeSqlSegment = new MergeSqlSegment();
        Iterator<Object> iterator = Arrays.asList(params).iterator();
        while (iterator.hasNext()) {
            Object nextArg = iterator.next();
            int sqlType = Types.OTHER;
            TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getDefaultTypeHandler();

            if (nextArg != null) {
                sqlType = TypeHandlerRegistry.toSqlType(nextArg.getClass());
                typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(nextArg.getClass());
            }

            Object arg = new SqlArg(nextArg, sqlType, exampleIsMap() ? null : typeHandler);
            mergeSqlSegment.addSegment(formatSegment(colValue, arg));
            if (iterator.hasNext()) {
                mergeSqlSegment.addSegment(d -> ",");
            }
        }
        return mergeSqlSegment;
    }

    protected Segment formatSegment(String argTemp, Object param) {
        return d -> format(argTemp, param);
    }

    private String format(String argTemp, Object param) {
        this.queryParam.add(param);
        return argTemp;
    }

    @Override
    public R eq(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            if (value == null) {
                return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.IS_NULL);
            } else {
                return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.EQ, formatValue(propertyName, value));
            }
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R ne(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            if (value == null) {
                return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.IS_NOT_NULL);
            } else {
                return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.NE, formatValue(propertyName, value));
            }
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R gt(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.GT, formatValue(propertyName, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R ge(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.GE, formatValue(propertyName, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R lt(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.LT, formatValue(propertyName, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R le(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.LE, formatValue(propertyName, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R like(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.LIKE, formatLikeValue(propertyName, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R notLike(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(propertyName, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R likeRight(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.LIKE, formatLikeValue(propertyName, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R notLikeRight(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(propertyName, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R likeLeft(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.LIKE, formatLikeValue(propertyName, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R notLikeLeft(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(propertyName, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R isNull(boolean test, P property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(getPropertyName(property)), SqlKeyword.IS_NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R isNotNull(boolean test, P property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(getPropertyName(property)), SqlKeyword.IS_NOT_NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R in(boolean test, P property, Collection<?> value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.IN, SqlKeyword.LEFT, formatValue(propertyName, value.toArray()), SqlKeyword.RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R notIn(boolean test, P property, Collection<?> value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.NOT, SqlKeyword.IN, SqlKeyword.LEFT, formatValue(propertyName, value.toArray()), SqlKeyword.RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R rangeBetween(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.BETWEEN, formatValue(propertyName, value1), SqlKeyword.AND, formatValue(propertyName, value2));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R rangeNotBetween(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.NOT, SqlKeyword.BETWEEN, formatValue(propertyName, value1), SqlKeyword.AND, formatValue(propertyName, value2));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R rangeOpenOpen(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            Segment colName = buildConditionByProperty(propertyName);
            return this.addCondition(   //
                    SqlKeyword.LEFT,    //
                    formatValue(propertyName, value1), SqlKeyword.LT, colName,//
                    SqlKeyword.AND,     //
                    colName, SqlKeyword.LT, formatValue(propertyName, value2),//
                    SqlKeyword.RIGHT    //
            );
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R rangeNotOpenOpen(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            Segment colName = buildConditionByProperty(propertyName);
            return this.addCondition(   //
                    SqlKeyword.NOT,     //
                    SqlKeyword.LEFT,    //
                    formatValue(propertyName, value1), SqlKeyword.LT, colName,//
                    SqlKeyword.AND,     //
                    colName, SqlKeyword.LT, formatValue(propertyName, value2),//
                    SqlKeyword.RIGHT    //
            );
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R rangeOpenClosed(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            Segment colName = buildConditionByProperty(propertyName);
            return this.addCondition(   //
                    SqlKeyword.LEFT,    //
                    formatValue(propertyName, value1), SqlKeyword.LT, colName,//
                    SqlKeyword.AND,     //
                    colName, SqlKeyword.LE, formatValue(propertyName, value2),//
                    SqlKeyword.RIGHT    //
            );
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R rangeNotOpenClosed(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            Segment colName = buildConditionByProperty(propertyName);
            return this.addCondition(   //
                    SqlKeyword.NOT,     //
                    SqlKeyword.LEFT,    //
                    formatValue(propertyName, value1), SqlKeyword.LT, colName,//
                    SqlKeyword.AND,     //
                    colName, SqlKeyword.LE, formatValue(propertyName, value2),//
                    SqlKeyword.RIGHT    //
            );
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R rangeClosedOpen(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            Segment colName = buildConditionByProperty(propertyName);
            return this.addCondition(   //
                    SqlKeyword.LEFT,    //
                    formatValue(propertyName, value1), SqlKeyword.LE, colName,//
                    SqlKeyword.AND,     //
                    colName, SqlKeyword.LT, formatValue(propertyName, value2),//
                    SqlKeyword.RIGHT    //
            );
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R rangeNotClosedOpen(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            Segment colName = buildConditionByProperty(propertyName);
            return this.addCondition(   //
                    SqlKeyword.NOT,    //
                    SqlKeyword.LEFT,    //
                    formatValue(propertyName, value1), SqlKeyword.LE, colName,//
                    SqlKeyword.AND,     //
                    colName, SqlKeyword.LT, formatValue(propertyName, value2),//
                    SqlKeyword.RIGHT    //
            );
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R rangeClosedClosed(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            Segment colName = buildConditionByProperty(propertyName);
            return this.addCondition(   //
                    SqlKeyword.LEFT,    //
                    formatValue(propertyName, value1), SqlKeyword.LE, colName,//
                    SqlKeyword.AND,     //
                    colName, SqlKeyword.LE, formatValue(propertyName, value2),//
                    SqlKeyword.RIGHT    //
            );
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R rangeNotClosedClosed(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            Segment colName = buildConditionByProperty(propertyName);
            return this.addCondition(   //
                    SqlKeyword.NOT,    //
                    SqlKeyword.LEFT,    //
                    formatValue(propertyName, value1), SqlKeyword.LE, colName,//
                    SqlKeyword.AND,     //
                    colName, SqlKeyword.LE, formatValue(propertyName, value2),//
                    SqlKeyword.RIGHT    //
            );
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R eqBySample(T sample) {
        if (sample == null) {
            throw new NullPointerException("sample is null.");
        }

        if (exampleIsMap()) {
            return this.eqBySampleMap((Map<String, Object>) sample);
        }

        boolean hasCondition = false;
        TableMapping<?> tableMapping = this.getTableMapping();
        for (ColumnMapping property : tableMapping.getProperties()) {
            Object value = property.getHandler().get(sample);
            if (value != null) {
                if (!hasCondition) {
                    this.addCondition(SqlKeyword.LEFT);
                    this.nextSegmentPrefix = SqlKeyword.EMPTY;
                    hasCondition = true;
                }
                String propertyName = property.getProperty();
                this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.EQ, formatValue(propertyName, value));
            }
        }

        if (hasCondition) {
            this.nextSegmentPrefix = SqlKeyword.EMPTY;
            this.addCondition(SqlKeyword.RIGHT);
        }

        return this.getSelf();
    }

    @Override
    public R eqBySampleMap(Map<String, Object> sample) {
        if (sample == null) {
            throw new NullPointerException("sample is null.");
        }

        boolean hasCondition = false;
        TableMapping<?> tableMapping = this.getTableMapping();
        if (!tableMapping.getProperties().isEmpty()) {
            // use column def.
            for (ColumnMapping property : tableMapping.getProperties()) {
                String propertyName = property.getProperty();
                Object value = sample.get(propertyName);
                if (value != null) {
                    if (!hasCondition) {
                        this.addCondition(SqlKeyword.LEFT);
                        this.nextSegmentPrefix = SqlKeyword.EMPTY;
                        hasCondition = true;
                    }

                    this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.EQ, formatValue(propertyName, value));
                }
            }
        } else {
            // not found any column.
            for (String propertyName : sample.keySet()) {
                Object value = sample.get(propertyName);
                if (value != null) {
                    if (!hasCondition) {
                        this.addCondition(SqlKeyword.LEFT);
                        this.nextSegmentPrefix = SqlKeyword.EMPTY;
                        hasCondition = true;
                    }
                    this.addCondition(buildConditionByProperty(propertyName), SqlKeyword.EQ, formatValue(value));
                }
            }
        }

        if (hasCondition) {
            this.nextSegmentPrefix = SqlKeyword.EMPTY;
            this.addCondition(SqlKeyword.RIGHT);
        }

        return this.getSelf();
    }
}