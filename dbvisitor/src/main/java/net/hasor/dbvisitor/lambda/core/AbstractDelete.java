/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.core;
import java.sql.SQLException;
import java.util.Objects;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;

/**
 * 提供 lambda delete 基础能力。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-27
 */
public abstract class AbstractDelete<R, T, P> extends BasicQueryCompare<R, T, P> implements DeleteExecute<R> {
    private boolean allowEmptyWhere = false;

    public AbstractDelete(Class<?> exampleType, TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(exampleType, tableMapping, registry, jdbc, ctx);
    }

    @Override
    public R reset() {
        super.reset();
        this.allowEmptyWhere = false;
        return this.getSelf();
    }

    @Override
    public int doDelete() throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");

        BoundSql boundSql = getBoundSql();
        String sqlString = boundSql.getSqlString();

        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + sqlString + "].");
        }

        return this.jdbc.executeUpdate(sqlString, boundSql.getArgs());
    }

    @Override
    public R allowEmptyWhere() {
        this.allowEmptyWhere = true;
        return getSelf();
    }

    @Override
    public BoundSql getBoundSql() throws SQLException {
        return this.cmdBuilder.buildDelete(isQualifier(), this.allowEmptyWhere);
    }
}
