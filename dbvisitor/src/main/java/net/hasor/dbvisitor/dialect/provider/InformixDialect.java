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
 * Informix 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class InformixDialect extends AbstractSqlDialect implements PageSqlDialect {
    public static final SqlDialect DEFAULT = new InformixDialect();

    @Override
    public SqlCommandBuilder newBuilder() {
        return new InformixDialect();
    }

    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/informix.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "\"";
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        String sqlString = boundSql.getSqlString();
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));

        StringBuilder sb = new StringBuilder();
        List<Object> newParam = new ArrayList<>();
        sb.append("SELECT ");
        if (start > 0) {
            sb.append(" SKIP ? ");
            newParam.add(start);
        }
        if (limit > 0) {
            sb.append(" FIRST ? ");
            newParam.add(limit);
        }
        sb.append(" * FROM ( ");
        sb.append(sqlString);
        sb.append(" ) TEMP_T");

        paramArrays.addAll(0, newParam);
        return new BoundSql.BoundSqlObj(sb.toString(), paramArrays.toArray());
    }
}
