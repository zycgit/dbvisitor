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
package net.hasor.dbvisitor.lambda.support.entity;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.dbvisitor.lambda.EntityQueryOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.core.AbstractSelectLambda;
import net.hasor.dbvisitor.mapping.TableReader;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.util.Collection;

import static net.hasor.dbvisitor.lambda.segment.SqlKeyword.*;

/**
 * 提供 lambda query 能力。是 EntityQueryOperation 接口的实现类。
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public class SelectLambdaForEntity<T> extends AbstractSelectLambda<EntityQueryOperation<T>, T, SFunction<T>> implements EntityQueryOperation<T> {
    protected TableReader<T> tableReader;

    public SelectLambdaForEntity(Class<T> exampleType, TableMapping<T> tableMapping, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, jdbcTemplate);
        this.tableReader = tableMapping.toReader();
    }

    @Override
    protected EntityQueryOperation<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }

    @Override
    protected TableReader<T> getTableReader() {
        return this.tableReader;
    }

    // ----------------------------------------------------

    @Override
    public EntityQueryOperation<T> eq(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), EQ, formatValue(property, value));
    }

    @Override
    public EntityQueryOperation<T> ne(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), NE, formatValue(property, value));
    }

    @Override
    public EntityQueryOperation<T> gt(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), GT, formatValue(property, value));
    }

    @Override
    public EntityQueryOperation<T> ge(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), GE, formatValue(property, value));
    }

    @Override
    public EntityQueryOperation<T> lt(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), LT, formatValue(property, value));
    }

    @Override
    public EntityQueryOperation<T> le(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), LE, formatValue(property, value));
    }

    @Override
    public EntityQueryOperation<T> like(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
    }

    @Override
    public EntityQueryOperation<T> notLike(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), NOT, LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
    }

    @Override
    public EntityQueryOperation<T> likeRight(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
    }

    @Override
    public EntityQueryOperation<T> notLikeRight(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), NOT, LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
    }

    @Override
    public EntityQueryOperation<T> likeLeft(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), LIKE, formatLikeValue(property, SqlLike.LEFT, value));
    }

    @Override
    public EntityQueryOperation<T> notLikeLeft(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), NOT, LIKE, formatLikeValue(property, SqlLike.LEFT, value));
    }

    @Override
    public EntityQueryOperation<T> isNull(String property) {
        return this.addCondition(buildConditionByProperty(true, property), IS_NULL);
    }

    @Override
    public EntityQueryOperation<T> isNotNull(String property) {
        return this.addCondition(buildConditionByProperty(true, property), IS_NOT_NULL);
    }

    @Override
    public EntityQueryOperation<T> in(String property, Collection<?> value) {
        return this.addCondition(buildConditionByProperty(true, property), IN, LEFT, formatValue(property, value.toArray()), RIGHT);
    }

    @Override
    public EntityQueryOperation<T> notIn(String property, Collection<?> value) {
        return this.addCondition(buildConditionByProperty(true, property), NOT, IN, LEFT, formatValue(property, value.toArray()), RIGHT);
    }

    @Override
    public EntityQueryOperation<T> between(String property, Object value1, Object value2) {
        return this.addCondition(buildConditionByProperty(true, property), BETWEEN, formatValue(property, value1), AND, formatValue(property, value2));
    }

    @Override
    public EntityQueryOperation<T> notBetween(String property, Object value1, Object value2) {
        return this.addCondition(buildConditionByProperty(true, property), NOT, BETWEEN, formatValue(property, value1), AND, formatValue(property, value2));
    }
}
