/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.provider;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;

/**
 * Hive 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class HiveDialect extends AbstractSqlDialect implements PageSqlDialect {
    public static final SqlDialect DEFAULT = new HiveDialect();

    @Override
    public SqlCommandBuilder newBuilder() {
        return new HiveDialect();
    }

    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/hive.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "\"";
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        throw new UnsupportedOperationException();
    }
}
