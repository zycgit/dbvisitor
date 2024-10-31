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
package net.hasor.dbvisitor.lambda.support.map;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.MapQueryOperation;
import net.hasor.dbvisitor.lambda.core.AbstractSelectLambda;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.util.Map;

/**
 * 提供 lambda query 能力，是 MapQueryOperation 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-04-02
 */
public class SelectLambdaForMap extends AbstractSelectLambda<MapQueryOperation, Map<String, Object>, String> //
        implements MapQueryOperation {

    public SelectLambdaForMap(TableMapping<?> tableMapping, RegistryManager registry, JdbcTemplate jdbc) {
        super(Map.class, tableMapping, registry, jdbc);
    }

    @Override
    protected MapQueryOperation getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(String property) {
        return property;
    }

    @Override
    public MapQueryOperation select(String property1) {
        return this.select(new String[] { property1 });
    }

    @Override
    public MapQueryOperation select(String property1, String property2) {
        return this.select(new String[] { property1, property2 });
    }

    @Override
    public MapQueryOperation select(String property1, String property2, String property3) {
        return this.select(new String[] { property1, property2, property3 });
    }

    @Override
    public MapQueryOperation select(String property1, String property2, String property3, String property4) {
        return this.select(new String[] { property1, property2, property3, property4 });
    }

    @Override
    public MapQueryOperation select(String property1, String property2, String property3, String property4, String property5) {
        return this.select(new String[] { property1, property2, property3, property4, property5 });
    }

    @Override
    public MapQueryOperation groupBy(String property1) {
        return this.groupBy(new String[] { property1 });
    }

    @Override
    public MapQueryOperation groupBy(String property1, String property2) {
        return this.groupBy(new String[] { property1, property2 });
    }

    @Override
    public MapQueryOperation groupBy(String property1, String property2, String property3) {
        return this.groupBy(new String[] { property1, property2, property3 });
    }

    @Override
    public MapQueryOperation groupBy(String property1, String property2, String property3, String property4) {
        return this.groupBy(new String[] { property1, property2, property3, property4 });
    }

    @Override
    public MapQueryOperation groupBy(String property1, String property2, String property3, String property4, String property5) {
        return this.groupBy(new String[] { property1, property2, property3, property4, property5 });
    }

    @Override
    public MapQueryOperation orderBy(String property1) {
        return this.orderBy(new String[] { property1 });
    }

    @Override
    public MapQueryOperation orderBy(String property1, String property2) {
        return this.orderBy(new String[] { property1, property2 });
    }

    @Override
    public MapQueryOperation orderBy(String property1, String property2, String property3) {
        return this.orderBy(new String[] { property1, property2, property3 });
    }

    @Override
    public MapQueryOperation orderBy(String property1, String property2, String property3, String property4) {
        return this.orderBy(new String[] { property1, property2, property3, property4 });
    }

    @Override
    public MapQueryOperation orderBy(String property1, String property2, String property3, String property4, String property5) {
        return this.orderBy(new String[] { property1, property2, property3, property4, property5 });
    }

    @Override
    public MapQueryOperation asc(String property1) {
        return this.asc(new String[] { property1 });
    }

    @Override
    public MapQueryOperation asc(String property1, String property2) {
        return this.asc(new String[] { property1, property2 });
    }

    @Override
    public MapQueryOperation asc(String property1, String property2, String property3) {
        return this.asc(new String[] { property1, property2, property3 });
    }

    @Override
    public MapQueryOperation asc(String property1, String property2, String property3, String property4) {
        return this.asc(new String[] { property1, property2, property3, property4 });
    }

    @Override
    public MapQueryOperation asc(String property1, String property2, String property3, String property4, String property5) {
        return this.asc(new String[] { property1, property2, property3, property4, property5 });
    }

    @Override
    public MapQueryOperation desc(String property1) {
        return this.desc(new String[] { property1 });
    }

    @Override
    public MapQueryOperation desc(String property1, String property2) {
        return this.desc(new String[] { property1, property2 });
    }

    @Override
    public MapQueryOperation desc(String property1, String property2, String property3) {
        return this.desc(new String[] { property1, property2, property3 });
    }

    @Override
    public MapQueryOperation desc(String property1, String property2, String property3, String property4) {
        return this.desc(new String[] { property1, property2, property3, property4 });
    }

    @Override
    public MapQueryOperation desc(String property1, String property2, String property3, String property4, String property5) {
        return this.desc(new String[] { property1, property2, property3, property4, property5 });
    }
}