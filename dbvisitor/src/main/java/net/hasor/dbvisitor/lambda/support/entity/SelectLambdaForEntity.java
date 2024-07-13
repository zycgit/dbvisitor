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
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;

import java.util.Collection;

import static net.hasor.dbvisitor.lambda.segment.SqlKeyword.*;

/**
 * 提供 lambda query 能力。是 EntityQueryOperation<T> 接口的实现类。
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public class SelectLambdaForEntity<T> extends AbstractSelectLambda<EntityQueryOperation<T>, T, SFunction<T>> implements EntityQueryOperation<T> {
    protected TableReader<T> tableReader;

    public SelectLambdaForEntity(Class<T> exampleType, TableMapping<T> tableMapping, MappingOptions opt, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, opt, jdbcTemplate);
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

    @Override
    public EntityQueryOperation<T> select(SFunction<T> property1) {
        return this.select(new SFunction[] { property1 });
    }

    @Override
    public EntityQueryOperation<T> select(SFunction<T> property1, SFunction<T> property2) {
        return this.select(new SFunction[] { property1, property2 });
    }

    @Override
    public EntityQueryOperation<T> select(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3) {
        return this.select(new SFunction[] { property1, property2, property3 });
    }

    @Override
    public EntityQueryOperation<T> select(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3, SFunction<T> property4) {
        return this.select(new SFunction[] { property1, property2, property3, property4 });
    }

    @Override
    public EntityQueryOperation<T> select(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3, SFunction<T> property4, SFunction<T> property5) {
        return this.select(new SFunction[] { property1, property2, property3, property4, property5 });
    }

    @Override
    public EntityQueryOperation<T> groupBy(SFunction<T> property1) {
        return this.groupBy(new SFunction[] { property1 });
    }

    @Override
    public EntityQueryOperation<T> groupBy(SFunction<T> property1, SFunction<T> property2) {
        return this.groupBy(new SFunction[] { property1, property2 });
    }

    @Override
    public EntityQueryOperation<T> groupBy(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3) {
        return this.groupBy(new SFunction[] { property1, property2, property3 });
    }

    @Override
    public EntityQueryOperation<T> groupBy(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3, SFunction<T> property4) {
        return this.groupBy(new SFunction[] { property1, property2, property3, property4 });
    }

    @Override
    public EntityQueryOperation<T> groupBy(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3, SFunction<T> property4, SFunction<T> property5) {
        return this.groupBy(new SFunction[] { property1, property2, property3, property4, property5 });
    }

    @Override
    public EntityQueryOperation<T> orderBy(SFunction<T> property1) {
        return this.orderBy(new SFunction[] { property1 });
    }

    @Override
    public EntityQueryOperation<T> orderBy(SFunction<T> property1, SFunction<T> property2) {
        return this.orderBy(new SFunction[] { property1, property2 });
    }

    @Override
    public EntityQueryOperation<T> orderBy(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3) {
        return this.orderBy(new SFunction[] { property1, property2, property3 });
    }

    @Override
    public EntityQueryOperation<T> orderBy(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3, SFunction<T> property4) {
        return this.orderBy(new SFunction[] { property1, property2, property3, property4 });
    }

    @Override
    public EntityQueryOperation<T> orderBy(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3, SFunction<T> property4, SFunction<T> property5) {
        return this.orderBy(new SFunction[] { property1, property2, property3, property4, property5 });
    }

    @Override
    public EntityQueryOperation<T> asc(SFunction<T> property1) {
        return this.asc(new SFunction[] { property1 });
    }

    @Override
    public EntityQueryOperation<T> asc(SFunction<T> property1, SFunction<T> property2) {
        return this.asc(new SFunction[] { property1, property2 });
    }

    @Override
    public EntityQueryOperation<T> asc(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3) {
        return this.asc(new SFunction[] { property1, property2, property3 });
    }

    @Override
    public EntityQueryOperation<T> asc(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3, SFunction<T> property4) {
        return this.asc(new SFunction[] { property1, property2, property3, property4 });
    }

    @Override
    public EntityQueryOperation<T> asc(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3, SFunction<T> property4, SFunction<T> property5) {
        return this.asc(new SFunction[] { property1, property2, property3, property4, property5 });
    }

    @Override
    public EntityQueryOperation<T> desc(SFunction<T> property1) {
        return this.desc(new SFunction[] { property1 });
    }

    @Override
    public EntityQueryOperation<T> desc(SFunction<T> property1, SFunction<T> property2) {
        return this.desc(new SFunction[] { property1, property2 });
    }

    @Override
    public EntityQueryOperation<T> desc(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3) {
        return this.desc(new SFunction[] { property1, property2, property3 });
    }

    @Override
    public EntityQueryOperation<T> desc(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3, SFunction<T> property4) {
        return this.desc(new SFunction[] { property1, property2, property3, property4 });
    }

    @Override
    public EntityQueryOperation<T> desc(SFunction<T> property1, SFunction<T> property2, SFunction<T> property3, SFunction<T> property4, SFunction<T> property5) {
        return this.desc(new SFunction[] { property1, property2, property3, property4, property5 });
    }

    // ----------------------------------------------------

    @Override
    public EntityQueryOperation<T> eq(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), EQ, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> ne(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), NE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> gt(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), GT, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> ge(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), GE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> lt(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), LT, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> le(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), LE, formatValue(property, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> like(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> notLike(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), NOT, LIKE, formatLikeValue(property, SqlLike.DEFAULT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> likeRight(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> notLikeRight(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), NOT, LIKE, formatLikeValue(property, SqlLike.RIGHT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> likeLeft(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), LIKE, formatLikeValue(property, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> notLikeLeft(boolean test, String property, Object value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), NOT, LIKE, formatLikeValue(property, SqlLike.LEFT, value));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> isNull(boolean test, String property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), IS_NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> isNotNull(boolean test, String property) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), IS_NOT_NULL);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> in(boolean test, String property, Collection<?> value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), IN, LEFT, formatValue(property, value.toArray()), RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> notIn(boolean test, String property, Collection<?> value) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), NOT, IN, LEFT, formatValue(property, value.toArray()), RIGHT);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> between(boolean test, String property, Object value1, Object value2) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), BETWEEN, formatValue(property, value1), AND, formatValue(property, value2));
        } else {
            return this.getSelf();
        }
    }

    @Override
    public EntityQueryOperation<T> notBetween(boolean test, String property, Object value1, Object value2) {
        if (test) {
            return this.addCondition(buildConditionByProperty(property), NOT, BETWEEN, formatValue(property, value1), AND, formatValue(property, value2));
        } else {
            return this.getSelf();
        }
    }
}
