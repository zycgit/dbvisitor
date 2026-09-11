/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.support.map;
import java.util.Map;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.MapUpdate;
import net.hasor.dbvisitor.lambda.core.AbstractUpdate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;

/**
 * 提供 lambda update 能力，是 MapUpdate 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public class MapUpdateImpl extends AbstractUpdate<MapUpdate, Map<String, Object>, String> //
        implements MapUpdate {

    public MapUpdateImpl(TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(Map.class, tableMapping, registry, jdbc, ctx);
    }

    @Override
    protected MapUpdate getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(String property) {
        return property;
    }
}
