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
package net.hasor.dbvisitor.wrapper.support.map;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.wrapper.MapQueryWrapper;
import net.hasor.dbvisitor.wrapper.core.AbstractSelectWrapper;
import net.hasor.dbvisitor.wrapper.core.OrderNullsStrategy;
import net.hasor.dbvisitor.wrapper.core.OrderType;

import java.util.Map;

/**
 * 提供 lambda query 能力，是 MapQueryOperation 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-04-02
 */
public class SelectWrapperForMap extends AbstractSelectWrapper<MapQueryWrapper, Map<String, Object>, String> //
        implements MapQueryWrapper {

    public SelectWrapperForMap(TableMapping<?> tableMapping, RegistryManager registry, JdbcTemplate jdbc) {
        super(Map.class, tableMapping, registry, jdbc);
    }

    @Override
    protected MapQueryWrapper getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(String property) {
        return property;
    }

    @Override
    public MapQueryWrapper select(String property) {
        return this.select(new String[] { property });
    }

    @Override
    public MapQueryWrapper selectAdd(String property) {
        return this.selectAdd(new String[] { property });
    }

    @Override
    public MapQueryWrapper groupBy(String property1) {
        return this.groupBy(new String[] { property1 });
    }

    @Override
    public MapQueryWrapper orderBy(String property1) {
        return this.addOrderBy(OrderType.DEFAULT, new String[] { property1 }, OrderNullsStrategy.DEFAULT);
    }

    @Override
    public MapQueryWrapper orderBy(String[] orderBy) {
        return this.addOrderBy(OrderType.DEFAULT, orderBy, OrderNullsStrategy.DEFAULT);
    }

    @Override
    public MapQueryWrapper orderBy(String property1, OrderType orderType, OrderNullsStrategy strategy) {
        return this.orderBy(new String[] { property1 }, orderType, strategy);
    }

    @Override
    public MapQueryWrapper orderBy(String[] properties, OrderType orderType, OrderNullsStrategy strategy) {
        switch (orderType) {
            case ASC:
                return this.addOrderBy(OrderType.ASC, properties, strategy);
            case DESC:
                return this.addOrderBy(OrderType.DESC, properties, strategy);
            case DEFAULT:
                return this.addOrderBy(OrderType.DEFAULT, properties, strategy);
            default:
                throw new UnsupportedOperationException("orderType " + orderType + " Unsupported.");
        }
    }

}