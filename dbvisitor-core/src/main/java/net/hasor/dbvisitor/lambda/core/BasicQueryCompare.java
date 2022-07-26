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
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;
import net.hasor.dbvisitor.lambda.segment.Segment;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.util.*;
import java.util.function.Consumer;

import static net.hasor.dbvisitor.lambda.segment.SqlKeyword.*;

/**
 * 扩展了 AbstractQueryExecute 提供 lambda 方式生成 SQL。 实现了 Compare 接口。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class BasicQueryCompare<R, T, P> extends BasicLambda<R, T, P> implements QueryCompare<R, P> {
    protected MergeSqlSegment queryTemplate     = new MergeSqlSegment();
    protected List<Object>    queryParam        = new ArrayList<>();
    private   Segment         nextSegmentPrefix = null;
    private   boolean         lockCondition     = false;

    public BasicQueryCompare(Class<?> exampleType, TableMapping<?> tableMapping, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, jdbcTemplate);
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
    public R or() {
        this.nextSegmentPrefix = OR;
        return this.getSelf();
    }

    @Override
    public R and() {
        this.nextSegmentPrefix = AND;
        return this.getSelf();
    }

    @Override
    public R apply(String sqlString, Object... args) {
        if (StringUtils.isBlank(sqlString)) {
            return this.getSelf();
        }
        this.queryTemplate.addSegment(() -> {
            if (args != null && args.length > 0) {
                for (Object arg : args) {
                    format(arg);
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

    protected Segment formatLikeValue(SqlLike like, Object param) {
        return () -> {
            format(param);
            return ((ConditionSqlDialect) this.dialect()).like(like, param);
        };
    }

    protected Segment formatValue(Object... params) {
        if (ArrayUtils.isEmpty(params)) {
            return () -> "";
        }

        MergeSqlSegment mergeSqlSegment = new MergeSqlSegment();
        Iterator<Object> iterator = Arrays.asList(params).iterator();
        while (iterator.hasNext()) {
            mergeSqlSegment.addSegment(formatSegment(iterator.next()));
            if (iterator.hasNext()) {
                mergeSqlSegment.addSegment(() -> ",");
            }
        }
        return mergeSqlSegment;
    }

    protected Segment formatSegment(Object param) {
        return () -> format(param);
    }

    protected String format(Object param) {
        this.queryParam.add(param);
        return "?";
    }

    @Override
    public R eq(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), EQ, formatValue(value));
    }

    @Override
    public R ne(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), NE, formatValue(value));
    }

    @Override
    public R gt(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), GT, formatValue(value));
    }

    @Override
    public R ge(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), GE, formatValue(value));
    }

    @Override
    public R lt(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), LT, formatValue(value));
    }

    @Override
    public R le(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), LE, formatValue(value));
    }

    @Override
    public R like(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), LIKE, formatLikeValue(SqlLike.DEFAULT, value));
    }

    @Override
    public R notLike(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), NOT, LIKE, formatLikeValue(SqlLike.DEFAULT, value));
    }

    @Override
    public R likeRight(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), LIKE, formatLikeValue(SqlLike.RIGHT, value));
    }

    @Override
    public R notLikeRight(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), NOT, LIKE, formatLikeValue(SqlLike.RIGHT, value));
    }

    @Override
    public R likeLeft(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), LIKE, formatLikeValue(SqlLike.LEFT, value));
    }

    @Override
    public R notLikeLeft(P property, Object value) {
        return this.addCondition(buildColumnByLambda(property), NOT, LIKE, formatLikeValue(SqlLike.LEFT, value));
    }

    @Override
    public R isNull(P property) {
        return this.addCondition(buildColumnByLambda(property), IS_NULL);
    }

    @Override
    public R isNotNull(P property) {
        return this.addCondition(buildColumnByLambda(property), IS_NOT_NULL);
    }

    @Override
    public R in(P property, Collection<?> value) {
        return this.addCondition(buildColumnByLambda(property), IN, LEFT, formatValue(value.toArray()), RIGHT);
    }

    @Override
    public R notIn(P property, Collection<?> value) {
        return this.addCondition(buildColumnByLambda(property), NOT, IN, LEFT, formatValue(value.toArray()), RIGHT);
    }

    @Override
    public R between(P property, Object value1, Object value2) {
        return this.addCondition(buildColumnByLambda(property), BETWEEN, formatValue(value1), AND, formatValue(value2));
    }

    @Override
    public R notBetween(P property, Object value1, Object value2) {
        return this.addCondition(buildColumnByLambda(property), NOT, BETWEEN, formatValue(value1), AND, formatValue(value2));
    }
}
