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
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;
import net.hasor.dbvisitor.lambda.segment.Segment;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.Types;
import java.util.*;
import java.util.function.Consumer;

import static net.hasor.dbvisitor.lambda.segment.SqlKeyword.*;

/**
 * 扩展了 AbstractQueryExecute 提供 lambda 方式生成 SQL。 实现了 Compare 接口。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-27
 */
public abstract class BasicQueryCompare<R, T, P> extends BasicLambda<R, T, P> implements QueryCompare<R, T, P> {
    protected MergeSqlSegment queryTemplate     = new MergeSqlSegment();
    protected List<Object>    queryParam        = new ArrayList<>();
    private   Segment         nextSegmentPrefix = null;
    private   boolean         lockCondition     = false;

    public BasicQueryCompare(Class<?> exampleType, TableMapping<?> tableMapping, RegistryManager registry, JdbcTemplate jdbc) {
        super(exampleType, tableMapping, registry, jdbc);
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
        this.addCondition(LEFT);
        this.nextSegmentPrefix = EMPTY;
        lambda.accept(this.getSelf());
        this.nextSegmentPrefix = EMPTY;
        this.addCondition(RIGHT);
        return this.getSelf();
    }

    @Override
    public R nested(boolean test, Consumer<R> lambda) {
        return test ? nested(lambda) : getSelf();
    }

    @Override
    public R or() {
        this.nextSegmentPrefix = OR;
        return this.getSelf();
    }

    @Override
    public R or(boolean test, Consumer<R> lambda) {
        return test ? or(lambda) : getSelf();
    }

    @Override
    public R and() {
        this.nextSegmentPrefix = AND;
        return this.getSelf();
    }

    @Override
    public R and(boolean test, Consumer<R> lambda) {
        return test ? and(lambda) : getSelf();
    }

    @Override
    public R apply(String sqlString, Object... args) {
        if (StringUtils.isBlank(sqlString)) {
            return this.getSelf();
        }
        this.queryTemplate.addSegment(d -> {
            if (args != null && args.length > 0) {
                for (Object arg : args) {
                    format("?", arg);
                }
            }
            return sqlString;
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

        if (this.nextSegmentPrefix == EMPTY) {
            this.nextSegmentPrefix = null;
        } else if (this.nextSegmentPrefix == null) {
            this.queryTemplate.addSegment(AND);
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
            return this.addCondition(buildConditionByProperty(propertyName), EQ, formatValue(propertyName, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R ne(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), NE, formatValue(propertyName, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R gt(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), GT, formatValue(propertyName, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R ge(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), GE, formatValue(propertyName, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R lt(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), LT, formatValue(propertyName, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R le(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), LE, formatValue(propertyName, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R like(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), LIKE, formatLikeValue(propertyName, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R notLike(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), NOT, LIKE, formatLikeValue(propertyName, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R likeRight(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), LIKE, formatLikeValue(propertyName, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R notLikeRight(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), NOT, LIKE, formatLikeValue(propertyName, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R likeLeft(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), LIKE, formatLikeValue(propertyName, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R notLikeLeft(boolean test, P property, Object value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), NOT, LIKE, formatLikeValue(propertyName, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R isNull(boolean test, P property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(getPropertyName(property)), IS_NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R isNotNull(boolean test, P property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(getPropertyName(property)), IS_NOT_NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R in(boolean test, P property, Collection<?> value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), IN, LEFT, formatValue(propertyName, value.toArray()), RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R notIn(boolean test, P property, Collection<?> value) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), NOT, IN, LEFT, formatValue(propertyName, value.toArray()), RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R between(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), BETWEEN, formatValue(propertyName, value1), AND, formatValue(propertyName, value2));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R notBetween(boolean test, P property, Object value1, Object value2) {
        if (test) {
            String propertyName = getPropertyName(property);
            return this.addCondition(buildConditionByProperty(propertyName), NOT, BETWEEN, formatValue(propertyName, value1), AND, formatValue(propertyName, value2));
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
                    this.addCondition(LEFT);
                    this.nextSegmentPrefix = EMPTY;
                    hasCondition = true;
                }
                String propertyName = property.getProperty();
                this.addCondition(buildConditionByProperty(propertyName), EQ, formatValue(propertyName, value));
            }
        }

        if (hasCondition) {
            this.nextSegmentPrefix = EMPTY;
            this.addCondition(RIGHT);
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
                        this.addCondition(LEFT);
                        this.nextSegmentPrefix = EMPTY;
                        hasCondition = true;
                    }

                    this.addCondition(buildConditionByProperty(propertyName), EQ, formatValue(propertyName, value));
                }
            }
        } else {
            // not found any column.
            for (String propertyName : sample.keySet()) {
                Object value = sample.get(propertyName);
                if (value != null) {
                    if (!hasCondition) {
                        this.addCondition(LEFT);
                        this.nextSegmentPrefix = EMPTY;
                        hasCondition = true;
                    }
                    this.addCondition(buildConditionByProperty(propertyName), EQ, formatValue(value));
                }
            }
        }

        if (hasCondition) {
            this.nextSegmentPrefix = EMPTY;
            this.addCondition(RIGHT);
        }

        return this.getSelf();
    }
}