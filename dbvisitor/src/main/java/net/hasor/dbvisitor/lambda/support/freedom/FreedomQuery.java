/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.support.freedom;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.support.map.MapQueryImpl;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;

/**
 * 提供 lambda query 能力。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public class FreedomQuery extends MapQueryImpl {
    public FreedomQuery(TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(tableMapping, registry, jdbc, ctx);
    }

    @Override
    protected boolean isFreedom() {
        return true;
    }

    @Override
    protected ColumnMapping whenPropertyNotExist(String propertyName) {
        return FreedomUtils.initOrGetMapMapping(propertyName, this.getTableMapping().isToCamelCase());
    }
}
