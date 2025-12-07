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
import java.util.Collection;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.ObjectUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.dbvisitor.dialect.builder.ConditionType;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.EntityUpdate;
import net.hasor.dbvisitor.lambda.MapUpdate;
import net.hasor.dbvisitor.lambda.core.AbstractUpdate;
import net.hasor.dbvisitor.lambda.support.map.MapUpdateImpl;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;

/**
 * 提供 lambda update 能力，是 EntityUpdate 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public class EntityUpdateImpl<T> extends AbstractUpdate<EntityUpdate<T>, T, SFunction<T>> implements EntityUpdate<T> {
    public EntityUpdateImpl(TableMapping<T> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(tableMapping.entityType(), tableMapping, registry, jdbc, ctx);
    }

    @Override
    public MapUpdate asMap() {
        return new MapUpdateImpl(this.getTableMapping(), this.registry, this.jdbc, this.queryContext);
    }

    @Override
    protected EntityUpdate<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }

    // ----------------------------------------------------

    @Override
    public EntityUpdate<T> eq(boolean test, String property, Object value) {
        if (test) {
            if (value == null) {
                this.addCondition(property, ConditionType.IS_NULL, null);
            } else {
                this.addCondition(property, ConditionType.EQ, value);
            }
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> ne(boolean test, String property, Object value) {
        if (test) {
            if (value == null) {
                this.addCondition(property, ConditionType.IS_NOT_NULL, null);
            } else {
                this.addCondition(property, ConditionType.NE, value);
            }
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> gt(boolean test, String property, Object value) {
        if (test) {
            this.addCondition(property, ConditionType.GT, value);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> ge(boolean test, String property, Object value) {
        if (test) {
            this.addCondition(property, ConditionType.GE, value);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> lt(boolean test, String property, Object value) {
        if (test) {
            this.addCondition(property, ConditionType.LT, value);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> le(boolean test, String property, Object value) {
        if (test) {
            this.addCondition(property, ConditionType.LE, value);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> like(boolean test, String property, Object value) {
        if (test) {
            this.addCondition(property, ConditionType.LIKE, value, SqlLike.DEFAULT);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> notLike(boolean test, String property, Object value) {
        if (test) {
            this.addCondition(property, ConditionType.NOT_LIKE, value, SqlLike.DEFAULT);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> likeRight(boolean test, String property, Object value) {
        if (test) {
            this.addCondition(property, ConditionType.LIKE, value, SqlLike.RIGHT);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> notLikeRight(boolean test, String property, Object value) {
        if (test) {
            this.addCondition(property, ConditionType.NOT_LIKE, value, SqlLike.RIGHT);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> likeLeft(boolean test, String property, Object value) {
        if (test) {
            this.addCondition(property, ConditionType.LIKE, value, SqlLike.LEFT);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> notLikeLeft(boolean test, String property, Object value) {
        if (test) {
            this.addCondition(property, ConditionType.NOT_LIKE, value, SqlLike.LEFT);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> isNull(boolean test, String property) {
        if (test) {
            this.addCondition(property, ConditionType.IS_NULL, null);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> isNotNull(boolean test, String property) {
        if (test) {
            this.addCondition(property, ConditionType.IS_NOT_NULL, null);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> in(boolean test, String property, Collection<?> value) {
        if (test) {
            ObjectUtils.assertTrue(!value.isEmpty(), "build in failed, value is empty.");
            this.addConditionForIn(property, ConditionType.IN, value);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> notIn(boolean test, String property, Collection<?> value) {
        if (test) {
            ObjectUtils.assertTrue(!value.isEmpty(), "build notIn failed, value is empty.");
            this.addConditionForIn(property, ConditionType.NOT_IN, value);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> between(boolean test, String property, Object value1, Object value2) {
        if (test) {
            this.addConditionForBetween(property, ConditionType.BETWEEN, value1, value2);
        }
        return this.getSelf();
    }

    @Override
    public EntityUpdate<T> notBetween(boolean test, String property, Object value1, Object value2) {
        if (test) {
            this.addConditionForBetween(property, ConditionType.NOT_BETWEEN, value1, value2);
        }
        return this.getSelf();
    }
}