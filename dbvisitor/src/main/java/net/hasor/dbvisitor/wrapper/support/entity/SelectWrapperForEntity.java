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
package net.hasor.dbvisitor.wrapper.support.entity;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.wrapper.EntityQueryWrapper;
import net.hasor.dbvisitor.wrapper.MapQueryWrapper;
import net.hasor.dbvisitor.wrapper.core.AbstractSelectWrapper;
import net.hasor.dbvisitor.wrapper.core.OrderNullsStrategy;
import net.hasor.dbvisitor.wrapper.core.OrderType;
import net.hasor.dbvisitor.wrapper.segment.SqlKeyword;
import net.hasor.dbvisitor.wrapper.support.map.SelectWrapperForMap;

import java.util.*;

/**
 * 提供 lambda query 能力。是 EntityQueryOperation<T> 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public class SelectWrapperForEntity<T> extends AbstractSelectWrapper<EntityQueryWrapper<T>, T, SFunction<T>> implements EntityQueryWrapper<T> {
    public SelectWrapperForEntity(TableMapping<T> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(tableMapping.entityType(), tableMapping, registry, jdbc, ctx);
    }

    @Override
    public MapQueryWrapper asMap() {
        return new SelectWrapperForMap(this.getTableMapping(), this.registry, this.jdbc, this.queryContext);
    }

    @Override
    protected EntityQueryWrapper<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }

    @SafeVarargs
    @Override
    public final EntityQueryWrapper<T> orderBy(OrderType orderType, OrderNullsStrategy strategy, SFunction<T> first, SFunction<T>... other) {
        List<SFunction<T>> orderBy;
        if (first == null && other == null) {
            throw new IndexOutOfBoundsException("properties is empty.");
        } else if (first != null && other != null) {
            orderBy = new ArrayList<>();
            orderBy.add(first);
            orderBy.addAll(Arrays.asList(other));
        } else if (first == null) {
            orderBy = Arrays.asList(other);
        } else {
            orderBy = Collections.singletonList(first);
        }

        switch (orderType) {
            case ASC:
                return this.addOrderBy(OrderType.ASC, orderBy, strategy);
            case DESC:
                return this.addOrderBy(OrderType.DESC, orderBy, strategy);
            case DEFAULT:
                return this.addOrderBy(OrderType.DEFAULT, orderBy, strategy);
            default:
                throw new UnsupportedOperationException("orderType " + orderType + " Unsupported.");
        }
    }

    // ----------------------------------------------------

    @Override
    public EntityQueryWrapper<T> eq(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.EQ, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> ne(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> gt(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.GT, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> ge(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.GE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> lt(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LT, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> le(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> like(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> notLike(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> likeRight(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> notLikeRight(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> likeLeft(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> notLikeLeft(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> isNull(boolean test, String property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IS_NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> isNotNull(boolean test, String property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IS_NOT_NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> in(boolean test, String property, Collection<?> value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IN, SqlKeyword.LEFT, formatValue(property, value.toArray()), SqlKeyword.RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> notIn(boolean test, String property, Collection<?> value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.IN, SqlKeyword.LEFT, formatValue(property, value.toArray()), SqlKeyword.RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> between(boolean test, String property, Object value1, Object value2) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.BETWEEN, formatValue(property, value1), SqlKeyword.AND, formatValue(property, value2));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryWrapper<T> notBetween(boolean test, String property, Object value1, Object value2) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.BETWEEN, formatValue(property, value1), SqlKeyword.AND, formatValue(property, value2));
        } else {
            return this.getSelf();
        }
    }
}
