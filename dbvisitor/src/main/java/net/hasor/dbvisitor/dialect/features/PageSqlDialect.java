/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.features;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlDialect;

/**
 * SQL 分页方言
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface PageSqlDialect extends SqlDialect {
    /** 生成 count 查询 SQL */
    default BoundSql countSql(BoundSql boundSql) {
        return new BoundSql.BoundSqlObj("SELECT COUNT(*) FROM (" + boundSql.getSqlString() + ") as TEMP_T", boundSql.getArgs());
    }

    /** 生成分页查询 SQL（基于 count 的） */
    BoundSql pageSql(BoundSql boundSql, long start, long limit);
}
