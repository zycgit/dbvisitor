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
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.template.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.wrapper.EntityDeleteWrapper;
import net.hasor.dbvisitor.wrapper.MapDeleteWrapper;
import net.hasor.dbvisitor.wrapper.core.AbstractDeleteWrapper;
import net.hasor.dbvisitor.wrapper.segment.SqlKeyword;
import net.hasor.dbvisitor.wrapper.support.map.DeleteWrapperForMap;

import java.util.Collection;

/**
 * 提供 lambda delete 能力，是 EntityDeleteOperation 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-04-02
 */
public class DeleteWrapperForEntity<T> extends AbstractDeleteWrapper<EntityDeleteWrapper<T>, T, SFunction<T>> implements EntityDeleteWrapper<T> {
    public DeleteWrapperForEntity(TableMapping<T> tableMapping, RegistryManager registry, JdbcTemplate jdbc) {
        super(tableMapping.entityType(), tableMapping, registry, jdbc);
    }

    @Override
    public MapDeleteWrapper asMap() {
        return new DeleteWrapperForMap(this.getTableMapping(), this.registry, this.jdbc);
    }

    @Override
    protected EntityDeleteWrapper<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }

    // ----------------------------------------------------

    @Override
    public EntityDeleteWrapper<T> eq(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.EQ, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> ne(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> gt(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.GT, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> ge(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.GE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> lt(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LT, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> le(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> like(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> notLike(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> likeRight(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> notLikeRight(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> likeLeft(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.LIKE, formatLikeValue(property, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> notLikeLeft(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.LIKE, formatLikeValue(property, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> isNull(boolean test, String property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IS_NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> isNotNull(boolean test, String property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IS_NOT_NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> in(boolean test, String property, Collection<?> value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.IN, SqlKeyword.LEFT, formatValue(property, value.toArray()), SqlKeyword.RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> notIn(boolean test, String property, Collection<?> value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.IN, SqlKeyword.LEFT, formatValue(property, value.toArray()), SqlKeyword.RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> between(boolean test, String property, Object value1, Object value2) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.BETWEEN, formatValue(property, value1), SqlKeyword.AND, formatValue(property, value2));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityDeleteWrapper<T> notBetween(boolean test, String property, Object value1, Object value2) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), SqlKeyword.NOT, SqlKeyword.BETWEEN, formatValue(property, value1), SqlKeyword.AND, formatValue(property, value2));
        } else {
            return this.getSelf();
        }
    }
}
