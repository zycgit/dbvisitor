/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.db.lambda.core;
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.db.dialect.BoundSql;
import net.hasor.db.dialect.ConditionSqlDialect;
import net.hasor.db.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.db.lambda.QueryCompare;
import net.hasor.db.lambda.segment.MergeSqlSegment;
import net.hasor.db.lambda.segment.Segment;
import net.hasor.db.mapping.def.ColumnMapping;
import net.hasor.db.mapping.def.TableMapping;

import java.util.*;
import java.util.function.Consumer;

import static net.hasor.db.lambda.segment.SqlKeyword.*;

/**
 * 扩展了 AbstractQueryExecute 提供 lambda 方式生成 SQL。 实现了 Compare 接口。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractQueryCompare<T, R> extends AbstractQueryExecute<T> implements QueryCompare<T, R> {
    protected MergeSqlSegment queryTemplate     = new MergeSqlSegment();
    protected List<Object>    queryParam        = new ArrayList<>();
    private   Segment         nextSegmentPrefix = null;
    private   boolean         lookCondition     = false;

    public AbstractQueryCompare(TableMapping<T> tableMapping, LambdaTemplate jdbcTemplate) {
        super(tableMapping, jdbcTemplate);
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
    public R nested(Consumer<QueryCompare<T, R>> lambda) {
        this.addCondition(LEFT);
        this.nextSegmentPrefix = EMPTY;
        lambda.accept(this);
        this.nextSegmentPrefix = EMPTY;
        this.addCondition(RIGHT);
        return this.getSelf();
    }

    public R eq(SFunction<T> property, Object value) {
        return this.eq(conditionName(property), value);
    }

    public R eq(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), EQ, formatValue(value));
    }

    public R ne(SFunction<T> property, Object value) {
        return this.ne(conditionName(property), value);
    }

    public R ne(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), NE, formatValue(value));
    }

    public R gt(SFunction<T> property, Object value) {
        return this.gt(conditionName(property), value);
    }

    public R gt(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), GT, formatValue(value));
    }

    public R ge(SFunction<T> property, Object value) {
        return this.ge(conditionName(property), value);
    }

    public R ge(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), GE, formatValue(value));
    }

    public R lt(SFunction<T> property, Object value) {
        return this.lt(conditionName(property), value);
    }

    public R lt(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), LT, formatValue(value));
    }

    public R le(SFunction<T> property, Object value) {
        return this.le(conditionName(property), value);
    }

    public R le(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), LE, formatValue(value));
    }

    public R like(SFunction<T> property, Object value) {
        return this.like(conditionName(property), value);
    }

    public R like(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), LIKE, formatLikeValue(SqlLike.DEFAULT, value));
    }

    public R notLike(SFunction<T> property, Object value) {
        return this.notLike(conditionName(property), value);
    }

    public R notLike(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), NOT, LIKE, formatLikeValue(SqlLike.DEFAULT, value));
    }

    public R likeRight(SFunction<T> property, Object value) {
        return this.likeRight(conditionName(property), value);
    }

    public R likeRight(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), LIKE, formatLikeValue(SqlLike.RIGHT, value));
    }

    public R notLikeRight(SFunction<T> property, Object value) {
        return this.notLikeRight(conditionName(property), value);
    }

    public R notLikeRight(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), NOT, LIKE, formatLikeValue(SqlLike.RIGHT, value));
    }

    public R likeLeft(SFunction<T> property, Object value) {
        return this.likeLeft(conditionName(property), value);
    }

    public R likeLeft(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), LIKE, formatLikeValue(SqlLike.LEFT, value));
    }

    public R notLikeLeft(SFunction<T> property, Object value) {
        return this.notLikeLeft(conditionName(property), value);
    }

    public R notLikeLeft(String column, Object value) {
        return this.addCondition(() -> fmtColumn(column), NOT, LIKE, formatLikeValue(SqlLike.LEFT, value));
    }

    public R isNull(SFunction<T> property) {
        return this.isNull(conditionName(property));
    }

    public R isNull(String column) {
        return this.addCondition(() -> fmtColumn(column), IS_NULL);
    }

    public R isNotNull(SFunction<T> property) {
        return this.isNotNull(conditionName(property));
    }

    public R isNotNull(String column) {
        return this.addCondition(() -> fmtColumn(column), IS_NOT_NULL);
    }

    public R in(SFunction<T> property, Collection<?> value) {
        return this.in(conditionName(property), value);
    }

    public R in(String column, Collection<?> value) {
        return this.addCondition(() -> fmtColumn(column), IN, LEFT, formatValue(value.toArray()), RIGHT);
    }

    public R notIn(SFunction<T> property, Collection<?> value) {
        return this.notIn(conditionName(property), value);
    }

    public R notIn(String column, Collection<?> value) {
        return this.addCondition(() -> fmtColumn(column), NOT, IN, LEFT, formatValue(value.toArray()), RIGHT);
    }

    public R between(SFunction<T> property, Object value1, Object value2) {
        return this.between(conditionName(property), value1, value2);
    }

    public R between(String column, Object value1, Object value2) {
        return this.addCondition(() -> fmtColumn(column), BETWEEN, formatValue(value1), AND, formatValue(value2));
    }

    public R notBetween(SFunction<T> property, Object value1, Object value2) {
        return this.notBetween(conditionName(property), value1, value2);
    }

    public R notBetween(String column, Object value1, Object value2) {
        return this.addCondition(() -> fmtColumn(column), NOT, BETWEEN, formatValue(value1), AND, formatValue(value2));
    }

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
        this.lookCondition = true;
    }

    protected final R addCondition(Segment... segments) {
        if (this.lookCondition) {
            throw new UnsupportedOperationException("condition is locked.");
        }
        //
        if (this.nextSegmentPrefix == EMPTY) {
            this.nextSegmentPrefix = null;
        } else if (this.nextSegmentPrefix == null) {
            this.queryTemplate.addSegment(AND);
            this.nextSegmentPrefix = null;
        } else {
            this.queryTemplate.addSegment(this.nextSegmentPrefix);
            this.nextSegmentPrefix = null;
        }
        //
        for (Segment segment : segments) {
            this.queryTemplate.addSegment(segment);
        }
        return this.getSelf();
    }

    protected abstract R getSelf();

    private Segment formatLikeValue(SqlLike like, Object param) {
        return () -> {
            format(param);
            return ((ConditionSqlDialect) this.dialect()).like(like, param);
        };
    }

    private Segment formatValue(Object... params) {
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

    protected String conditionName(SFunction<T> property) {
        TableMapping<T> tableDef = super.getTableMapping();
        ColumnMapping columnDef = tableDef.getPropertyByName(BeanUtils.toProperty(property));
        return this.dialect().columnName(isQualifier(), tableDef.getSchema(), tableDef.getTable(), columnDef.getColumn());
    }

    protected String fmtColumn(String columnName) {
        TableMapping<T> tableDef = super.getTableMapping();
        return this.dialect().columnName(isQualifier(), tableDef.getSchema(), tableDef.getTable(), columnName);
    }

    @Override
    public BoundSql getOriginalBoundSql() {
        return new BoundSql() {
            public String getSqlString() {
                return queryTemplate.noFirstSqlSegment();
            }

            public Object[] getArgs() {
                return queryParam.toArray().clone();
            }
        };
    }
}
