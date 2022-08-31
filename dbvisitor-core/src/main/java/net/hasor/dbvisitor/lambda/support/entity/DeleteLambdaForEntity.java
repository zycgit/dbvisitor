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
import net.hasor.dbvisitor.lambda.EntityDeleteOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.core.AbstractDeleteLambda;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.util.Collection;

import static net.hasor.dbvisitor.lambda.segment.SqlKeyword.*;

/**
 * 提供 lambda delete 能力，是 EntityDeleteOperation 接口的实现类。
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public class DeleteLambdaForEntity<T> extends AbstractDeleteLambda<EntityDeleteOperation<T>, T, SFunction<T>> implements EntityDeleteOperation<T> {

    public DeleteLambdaForEntity(Class<T> exampleType, TableMapping<T> tableMapping, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, jdbcTemplate);
    }

    @Override
    protected EntityDeleteOperation<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }

    // ----------------------------------------------------

    @Override
    public EntityDeleteOperation<T> eq(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), EQ, formatValue(property, value));
    }

    @Override
    public EntityDeleteOperation<T> ne(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), NE, formatValue(property, value));
    }

    @Override
    public EntityDeleteOperation<T> gt(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), GT, formatValue(property, value));
    }

    @Override
    public EntityDeleteOperation<T> ge(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), GE, formatValue(property, value));
    }

    @Override
    public EntityDeleteOperation<T> lt(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), LT, formatValue(property, value));
    }

    @Override
    public EntityDeleteOperation<T> le(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), LE, formatValue(property, value));
    }

    @Override
    public EntityDeleteOperation<T> like(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
    }

    @Override
    public EntityDeleteOperation<T> notLike(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), NOT, LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
    }

    @Override
    public EntityDeleteOperation<T> likeRight(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
    }

    @Override
    public EntityDeleteOperation<T> notLikeRight(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), NOT, LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
    }

    @Override
    public EntityDeleteOperation<T> likeLeft(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), LIKE, formatLikeValue(property, SqlLike.LEFT, value));
    }

    @Override
    public EntityDeleteOperation<T> notLikeLeft(String property, Object value) {
        return this.addCondition(buildConditionByProperty(true, property), NOT, LIKE, formatLikeValue(property, SqlLike.LEFT, value));
    }

    @Override
    public EntityDeleteOperation<T> isNull(String property) {
        return this.addCondition(buildConditionByProperty(true, property), IS_NULL);
    }

    @Override
    public EntityDeleteOperation<T> isNotNull(String property) {
        return this.addCondition(buildConditionByProperty(true, property), IS_NOT_NULL);
    }

    @Override
    public EntityDeleteOperation<T> in(String property, Collection<?> value) {
        return this.addCondition(buildConditionByProperty(true, property), IN, LEFT, formatValue(property, value.toArray()), RIGHT);
    }

    @Override
    public EntityDeleteOperation<T> notIn(String property, Collection<?> value) {
        return this.addCondition(buildConditionByProperty(true, property), NOT, IN, LEFT, formatValue(property, value.toArray()), RIGHT);
    }

    @Override
    public EntityDeleteOperation<T> between(String property, Object value1, Object value2) {
        return this.addCondition(buildConditionByProperty(true, property), BETWEEN, formatValue(property, value1), AND, formatValue(property, value2));
    }

    @Override
    public EntityDeleteOperation<T> notBetween(String property, Object value1, Object value2) {
        return this.addCondition(buildConditionByProperty(true, property), NOT, BETWEEN, formatValue(property, value1), AND, formatValue(property, value2));
    }
}
