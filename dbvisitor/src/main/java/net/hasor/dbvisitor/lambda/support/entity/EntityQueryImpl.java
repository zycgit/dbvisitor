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
import java.util.*;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.ObjectUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.MapQuery;
import net.hasor.dbvisitor.lambda.core.AbstractSelect;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.lambda.segment.SqlKeyword;
import net.hasor.dbvisitor.lambda.support.map.MapQueryImpl;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;

/**
 * 提供 lambda query 能力。是 EntityQuery 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public class EntityQueryImpl<T> extends AbstractSelect<EntityQuery<T>, T, SFunction<T>> implements EntityQuery<T> {
    public EntityQueryImpl(TableMapping<T> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(tableMapping.entityType(), tableMapping, registry, jdbc, ctx);
    }

    @Override
    public MapQuery asMap() {
        return new MapQueryImpl(this.getTableMapping(), this.registry, this.jdbc, this.queryContext);
    }

    @Override
    protected EntityQuery<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }

    @SafeVarargs
    @Override
    public final EntityQuery<T> groupBy(String first, String... other) {
        if (this.lockGroupBy) {
            throw new IllegalStateException("must before order by invoke it.");
        }

        lockCondition();

        //
        List<String> groupBy;
        if (first == null && other == null) {
            throw new IndexOutOfBoundsException("properties is empty.");
        } else if (first != null && other != null) {
            groupBy = new ArrayList<>();
            groupBy.add(first);
            groupBy.addAll(Arrays.asList(other));
        } else if (first == null) {
            groupBy = Arrays.asList(other);
        } else {
            groupBy = Collections.singletonList(first);
        }

        //
        if (!groupBy.isEmpty()) {
            if (this.groupByList.isEmpty()) {
                this.queryTemplate.addSegment(SqlKeyword.GROUP_BY);
                this.queryTemplate.addSegment(this.groupByList);
            }

            for (String property : groupBy) {
                if (!this.groupByList.isEmpty()) {
                    this.groupByList.addSegment(d -> ",");
                }
                this.groupByList.addSegment(buildGroupByProperty(property));

            }
        }
        return this.getSelf();
    }

    @SafeVarargs
    @Override
    public final EntityQuery<T> orderBy(OrderType orderType, OrderNullsStrategy strategy, SFunction<T> first, SFunction<T>... other) {
        List<String> orderBy;
        if (first == null && other == null) {
            throw new IndexOutOfBoundsException("properties is empty.");
        } else if (first != null && other != null) {
            orderBy = new ArrayList<>();
            orderBy.add(getPropertyName(first));
            Arrays.stream(other).map(this::getPropertyName).forEach(orderBy::add);
        } else if (first == null) {
            orderBy = new ArrayList<>();
            Arrays.stream(other).map(this::getPropertyName).forEach(orderBy::add);
        } else {
            orderBy = Collections.singletonList(getPropertyName(first));
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

    @Override
    public EntityQuery<T> orderBy(OrderType orderType, OrderNullsStrategy strategy, String first, String... other) {
        List<String> orderBy;
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
    public EntityQuery<T> eq(boolean test, String property, Object value) {
        if (test) {
            if (value == null) {
                return this.addCondition(buildConditionByProperty(property), SqlKeyword.IS, SqlKeyword.NULL);
            } else {
                return this.addCondition(buildConditionByProperty(property), SqlKeyword.EQ, formatValue(property, value));
            }
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> ne(boolean test, String property, Object value) {
        if (test) {
            if (value == null) {
                return this.addCondition(buildConditionByProperty(property), SqlKeyword.IS, SqlKeyword.NOT, SqlKeyword.NULL);
            } else {
                return this.addCondition(buildConditionByProperty(property), SqlKeyword.NE, formatValue(property, value));
            }
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> gt(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.GT, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> ge(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.GE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> lt(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LT, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> le(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> like(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> notLike(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> likeRight(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> notLikeRight(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> likeLeft(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> notLikeLeft(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> isNull(boolean test, String property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IS, SqlKeyword.NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> isNotNull(boolean test, String property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IS, SqlKeyword.NOT, SqlKeyword.NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> in(boolean test, String property, Collection<?> value) {
        if (test) {
            ObjectUtils.assertTrue(!value.isEmpty(), "build in failed, value is empty.");
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IN, SqlKeyword.LEFT, formatValue(property, value.toArray()), SqlKeyword.RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> notIn(boolean test, String property, Collection<?> value) {
        if (test) {
            ObjectUtils.assertTrue(!value.isEmpty(), "build notIn failed, value is empty.");
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.IN, SqlKeyword.LEFT, formatValue(property, value.toArray()), SqlKeyword.RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> between(boolean test, String property, Object value1, Object value2) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.BETWEEN, formatValue(property, value1), SqlKeyword.AND, formatValue(property, value2));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQuery<T> notBetween(boolean test, String property, Object value1, Object value2) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.BETWEEN, formatValue(property, value1), SqlKeyword.AND, formatValue(property, value2));
        } else {
            return this.getSelf();
        }
    }

    @SafeVarargs
    @Override
    public final EntityQuery<T> selectAdd(String first, String... other) {
        if (first == null && other == null) {
            throw new IndexOutOfBoundsException("properties is empty.");
        } else if (first != null && other != null) {
            List<String> list = new ArrayList<>();
            list.add(first);
            list.addAll(Arrays.asList(other));
            return this.selectApply(list, false);
        } else if (first == null) {
            return this.selectApply(Arrays.asList(other), false);
        } else {
            return this.selectApply(Collections.singletonList(first), false);
        }
    }

    @SafeVarargs
    @Override
    public final EntityQuery<T> select(String first, String... other) {
        if (first == null && other == null) {
            throw new IndexOutOfBoundsException("properties is empty.");
        } else if (first != null && other != null) {
            List<String> list = new ArrayList<>();
            list.add(first);
            list.addAll(Arrays.asList(other));
            return this.selectApply(list, true);
        } else if (first == null) {
            return this.selectApply(Arrays.asList(other), true);
        } else {
            return this.selectApply(Collections.singletonList(first), true);
        }
    }
}
