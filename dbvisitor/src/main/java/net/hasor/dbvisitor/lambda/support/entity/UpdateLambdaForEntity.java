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
import net.hasor.dbvisitor.lambda.EntityUpdateOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.core.AbstractUpdateLambda;
import net.hasor.dbvisitor.mapping.TableMapping;

import java.util.Collection;

import static net.hasor.dbvisitor.lambda.segment.SqlKeyword.*;

/**
 * 提供 lambda update 能力，是 EntityUpdateOperation 接口的实现类。
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public class UpdateLambdaForEntity<T> extends AbstractUpdateLambda<EntityUpdateOperation<T>, T, SFunction<T>> implements EntityUpdateOperation<T> {
    public UpdateLambdaForEntity(Class<T> exampleType, TableMapping<T> tableMapping, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, jdbcTemplate);
    }

    @Override
    protected EntityUpdateOperation<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }

    // ----------------------------------------------------

    @Override
    public EntityUpdateOperation<T> eq(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), EQ, formatValue(property, value));
    }

    @Override
    public EntityUpdateOperation<T> ne(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), NE, formatValue(property, value));
    }

    @Override
    public EntityUpdateOperation<T> gt(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), GT, formatValue(property, value));
    }

    @Override
    public EntityUpdateOperation<T> ge(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), GE, formatValue(property, value));
    }

    @Override
    public EntityUpdateOperation<T> lt(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), LT, formatValue(property, value));
    }

    @Override
    public EntityUpdateOperation<T> le(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), LE, formatValue(property, value));
    }

    @Override
    public EntityUpdateOperation<T> like(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
    }

    @Override
    public EntityUpdateOperation<T> notLike(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), NOT, LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
    }

    @Override
    public EntityUpdateOperation<T> likeRight(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
    }

    @Override
    public EntityUpdateOperation<T> notLikeRight(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), NOT, LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
    }

    @Override
    public EntityUpdateOperation<T> likeLeft(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), LIKE, formatLikeValue(property, SqlLike.LEFT, value));
    }

    @Override
    public EntityUpdateOperation<T> notLikeLeft(String property, Object value) {
        return this.addCondition(buildConditionByProperty(property), NOT, LIKE, formatLikeValue(property, SqlLike.LEFT, value));
    }

    @Override
    public EntityUpdateOperation<T> isNull(String property) {
        return this.addCondition(buildConditionByProperty(property), IS_NULL);
    }

    @Override
    public EntityUpdateOperation<T> isNotNull(String property) {
        return this.addCondition(buildConditionByProperty(property), IS_NOT_NULL);
    }

    @Override
    public EntityUpdateOperation<T> in(String property, Collection<?> value) {
        return this.addCondition(buildConditionByProperty(property), IN, LEFT, formatValue(property, value.toArray()), RIGHT);
    }

    @Override
    public EntityUpdateOperation<T> notIn(String property, Collection<?> value) {
        return this.addCondition(buildConditionByProperty(property), NOT, IN, LEFT, formatValue(property, value.toArray()), RIGHT);
    }

    @Override
    public EntityUpdateOperation<T> between(String property, Object value1, Object value2) {
        return this.addCondition(buildConditionByProperty(property), BETWEEN, formatValue(property, value1), AND, formatValue(property, value2));
    }

    @Override
    public EntityUpdateOperation<T> notBetween(String property, Object value1, Object value2) {
        return this.addCondition(buildConditionByProperty(property), NOT, BETWEEN, formatValue(property, value1), AND, formatValue(property, value2));
    }
}