/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;

/**
 * 虚谷数据库的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class XuGuDialect extends AbstractSqlDialect implements PageSqlDialect {
    public static final SqlDialect DEFAULT = new XuGuDialect();

    @Override
    public SqlCommandBuilder newBuilder() {
        return new XuGuDialect();
    }

    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/xugu.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "`";
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sb = new StringBuilder(boundSql.getSqlString());
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));

        if (start <= 0) {
            sb.append(" LIMIT ?");
            paramArrays.add(limit);
        } else {
            sb.append(" LIMIT ?, ?");
            paramArrays.add(start);
            paramArrays.add(limit);
        }

        return new BoundSql.BoundSqlObj(sb.toString(), paramArrays.toArray());
    }
}
