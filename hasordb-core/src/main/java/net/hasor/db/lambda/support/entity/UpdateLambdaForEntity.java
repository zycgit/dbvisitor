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
package net.hasor.db.lambda.support.entity;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.db.dialect.ConditionSqlDialect;
import net.hasor.db.lambda.EntityUpdateOperation;
import net.hasor.db.lambda.LambdaTemplate;
import net.hasor.db.lambda.core.AbstractUpdateLambda;
import net.hasor.db.mapping.def.TableMapping;

import java.util.Collection;

import static net.hasor.db.lambda.segment.SqlKeyword.*;

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
        return this.addCondition(buildColumnByProperty(property), EQ, formatValue(value));
    }

    @Override
    public EntityUpdateOperation<T> ne(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), NE, formatValue(value));
    }

    @Override
    public EntityUpdateOperation<T> gt(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), GT, formatValue(value));
    }

    @Override
    public EntityUpdateOperation<T> ge(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), GE, formatValue(value));
    }

    @Override
    public EntityUpdateOperation<T> lt(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), LT, formatValue(value));
    }

    @Override
    public EntityUpdateOperation<T> le(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), LE, formatValue(value));
    }

    @Override
    public EntityUpdateOperation<T> like(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.DEFAULT, value));
    }

    @Override
    public EntityUpdateOperation<T> notLike(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), NOT, LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.DEFAULT, value));
    }

    @Override
    public EntityUpdateOperation<T> likeRight(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.RIGHT, value));
    }

    @Override
    public EntityUpdateOperation<T> notLikeRight(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), NOT, LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.RIGHT, value));
    }

    @Override
    public EntityUpdateOperation<T> likeLeft(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.LEFT, value));
    }

    @Override
    public EntityUpdateOperation<T> notLikeLeft(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), NOT, LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.LEFT, value));
    }

    @Override
    public EntityUpdateOperation<T> isNull(String property) {
        return this.addCondition(buildColumnByProperty(property), IS_NULL);
    }

    @Override
    public EntityUpdateOperation<T> isNotNull(String property) {
        return this.addCondition(buildColumnByProperty(property), IS_NOT_NULL);
    }

    @Override
    public EntityUpdateOperation<T> in(String property, Collection<?> value) {
        return this.addCondition(buildColumnByProperty(property), IN, LEFT, formatValue(value.toArray()), RIGHT);
    }

    @Override
    public EntityUpdateOperation<T> notIn(String property, Collection<?> value) {
        return this.addCondition(buildColumnByProperty(property), NOT, IN, LEFT, formatValue(value.toArray()), RIGHT);
    }

    @Override
    public EntityUpdateOperation<T> between(String property, Object value1, Object value2) {
        return this.addCondition(buildColumnByProperty(property), BETWEEN, formatValue(value1), AND, formatValue(value2));
    }

    @Override
    public EntityUpdateOperation<T> notBetween(String property, Object value1, Object value2) {
        return this.addCondition(buildColumnByProperty(property), NOT, BETWEEN, formatValue(value1), AND, formatValue(value2));
    }
}