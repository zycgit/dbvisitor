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
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.MapQuery;
import net.hasor.dbvisitor.lambda.core.AbstractSelect;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.util.*;

/**
 * 提供 lambda query 能力，是 MapQuery 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public class MapQueryImpl extends AbstractSelect<MapQuery, Map<String, Object>, String> //
        implements MapQuery {

    public MapQueryImpl(TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(Map.class, tableMapping, registry, jdbc, ctx);
    }

    @Override
    protected MapQuery getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(String property) {
        return property;
    }

    @SafeVarargs
    @Override
    public final MapQuery orderBy(OrderType orderType, OrderNullsStrategy strategy, String first, String... other) {
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
}