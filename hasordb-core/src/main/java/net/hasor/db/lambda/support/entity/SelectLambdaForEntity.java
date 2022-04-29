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
import net.hasor.db.lambda.EntityQueryOperation;
import net.hasor.db.lambda.LambdaTemplate;
import net.hasor.db.lambda.core.AbstractSelectLambda;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.mapping.def.TableMapping;

import java.util.Collection;

import static net.hasor.db.lambda.segment.SqlKeyword.*;

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
        return this.addCondition(buildColumnByProperty(property), EQ, formatValue(value));
    }

    @Override
    public EntityQueryOperation<T> ne(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), NE, formatValue(value));
    }

    @Override
    public EntityQueryOperation<T> gt(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), GT, formatValue(value));
    }

    @Override
    public EntityQueryOperation<T> ge(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), GE, formatValue(value));
    }

    @Override
    public EntityQueryOperation<T> lt(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), LT, formatValue(value));
    }

    @Override
    public EntityQueryOperation<T> le(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), LE, formatValue(value));
    }

    @Override
    public EntityQueryOperation<T> like(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.DEFAULT, value));
    }

    @Override
    public EntityQueryOperation<T> notLike(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), NOT, LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.DEFAULT, value));
    }

    @Override
    public EntityQueryOperation<T> likeRight(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.RIGHT, value));
    }

    @Override
    public EntityQueryOperation<T> notLikeRight(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), NOT, LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.RIGHT, value));
    }

    @Override
    public EntityQueryOperation<T> likeLeft(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.LEFT, value));
    }

    @Override
    public EntityQueryOperation<T> notLikeLeft(String property, Object value) {
        return this.addCondition(buildColumnByProperty(property), NOT, LIKE, formatLikeValue(ConditionSqlDialect.SqlLike.LEFT, value));
    }

    @Override
    public EntityQueryOperation<T> isNull(String property) {
        return this.addCondition(buildColumnByProperty(property), IS_NULL);
    }

    @Override
    public EntityQueryOperation<T> isNotNull(String property) {
        return this.addCondition(buildColumnByProperty(property), IS_NOT_NULL);
    }

    @Override
    public EntityQueryOperation<T> in(String property, Collection<?> value) {
        return this.addCondition(buildColumnByProperty(property), IN, LEFT, formatValue(value.toArray()), RIGHT);
    }

    @Override
    public EntityQueryOperation<T> notIn(String property, Collection<?> value) {
        return this.addCondition(buildColumnByProperty(property), NOT, IN, LEFT, formatValue(value.toArray()), RIGHT);
    }

    @Override
    public EntityQueryOperation<T> between(String property, Object value1, Object value2) {
        return this.addCondition(buildColumnByProperty(property), BETWEEN, formatValue(value1), AND, formatValue(value2));
    }

    @Override
    public EntityQueryOperation<T> notBetween(String property, Object value1, Object value2) {
        return this.addCondition(buildColumnByProperty(property), NOT, BETWEEN, formatValue(value1), AND, formatValue(value2));
    }
}
