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
import net.hasor.cobble.ObjectUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.EntityDelete;
import net.hasor.dbvisitor.lambda.MapDelete;
import net.hasor.dbvisitor.lambda.core.AbstractDelete;
import net.hasor.dbvisitor.lambda.segment.SqlKeyword;
import net.hasor.dbvisitor.lambda.support.map.MapDeleteImpl;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.util.Collection;

/**
 * 提供 lambda delete 能力，是 EntityDelete 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public class EntityDeleteImpl<T> extends AbstractDelete<EntityDelete<T>, T, SFunction<T>> implements EntityDelete<T> {
    public EntityDeleteImpl(TableMapping<T> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(tableMapping.entityType(), tableMapping, registry, jdbc, ctx);
    }

    @Override
    public MapDelete asMap() {
        return new MapDeleteImpl(this.getTableMapping(), this.registry, this.jdbc, this.queryContext);
    }

    @Override
    protected EntityDelete<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }

    // ----------------------------------------------------

    @Override
    public EntityDelete<T> eq(boolean test, String property, Object value) {
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
    public EntityDelete<T> ne(boolean test, String property, Object value) {
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
    public EntityDelete<T> gt(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.GT, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> ge(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.GE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> lt(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LT, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> le(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> like(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> notLike(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> likeRight(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> notLikeRight(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> likeLeft(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> notLikeLeft(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> isNull(boolean test, String property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IS, SqlKeyword.NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> isNotNull(boolean test, String property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IS, SqlKeyword.NOT, SqlKeyword.NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> in(boolean test, String property, Collection<?> value) {
        if (test) {
            ObjectUtils.assertTrue(!value.isEmpty(), "build in failed, value is empty.");
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IN, SqlKeyword.LEFT, formatValue(property, value.toArray()), SqlKeyword.RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> notIn(boolean test, String property, Collection<?> value) {
        if (test) {
            ObjectUtils.assertTrue(!value.isEmpty(), "build notIn failed, value is empty.");
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.IN, SqlKeyword.LEFT, formatValue(property, value.toArray()), SqlKeyword.RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> between(boolean test, String property, Object value1, Object value2) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.BETWEEN, formatValue(property, value1), SqlKeyword.AND, formatValue(property, value2));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDelete<T> notBetween(boolean test, String property, Object value1, Object value2) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.BETWEEN, formatValue(property, value1), SqlKeyword.AND, formatValue(property, value2));
        } else {
            return this.getSelf();
        }
    }
}
