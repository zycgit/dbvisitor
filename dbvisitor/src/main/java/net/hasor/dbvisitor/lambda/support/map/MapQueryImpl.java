/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.support.map;
import java.util.*;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.MapQuery;
import net.hasor.dbvisitor.lambda.core.AbstractSelect;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;

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
