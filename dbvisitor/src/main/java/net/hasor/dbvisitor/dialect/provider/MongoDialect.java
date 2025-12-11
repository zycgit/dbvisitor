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
package net.hasor.dbvisitor.dialect.provider;

import java.util.Collections;
import java.util.Set;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.builder.CommandBuilder;
import net.hasor.dbvisitor.dialect.builder.MongoCommandBuilder;

/**
 * MongoDB 方言实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-07
 */
public class MongoDialect extends AbstractDialect implements PageSqlDialect, ConditionSqlDialect {
    public Set<String> keywords() {
        return Collections.emptySet();
    }

    @Override
    public String tableName(boolean useQualifier, String catalog, String schema, String table) {
        String dbName = catalog;
        String collName = table;

        if (StringUtils.isBlank(catalog) && StringUtils.isNotBlank(schema)) {
            dbName = schema;
        }

        StringBuilder sb = new StringBuilder();
        if (StringUtils.isBlank(dbName)) {
            sb.append("db.");
        } else {
            sb.append(dbName).append(".");
        }
        sb.append(collName);
        return sb.toString();
    }

    @Override
    public String fmtName(boolean useQualifier, String name) {
        return name;
    }

    @Override
    protected String defaultQualifier() {
        return "";
    }

    @Override
    public String aliasSeparator() {
        return ":";
    }

    @Override
    public CommandBuilder newBuilder() {
        return new MongoCommandBuilder();
    }

    @Override
    public String like(SqlLike likeType, Object value, String valueTerm) {
        if (StringUtils.isNotBlank(valueTerm)) {
            return valueTerm;
        }

        String strVal = value == null ? "" : value.toString();
        switch (likeType) {
            case LEFT:
                return escapeRegex("^", strVal, "");
            case RIGHT:
                return escapeRegex("", strVal, "$");
            default:
                return escapeRegex("", strVal, "");
        }
    }

    private String escapeRegex(String begin, String input, String end) {
        if (input == null) {
            return "";
        }
        if (!begin.isEmpty() && !StringUtils.startsWith(input, begin)) {
            input = begin + input;
        }
        if (!begin.isEmpty() && !StringUtils.endsWith(input, end)) {
            input = input + end;
        }
        return input;
    }

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        return new BoundSql.BoundSqlObj("/*+override_find_as_count*/" + boundSql.getSqlString(), boundSql.getArgs());
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sqlBuilder = new StringBuilder("/*+");

        if (start <= 0) {
            sqlBuilder.append("override_find_limit=" + limit);
        } else {
            sqlBuilder.append("override_find_skip=" + start + ",override_find_limit=" + limit);
        }

        sqlBuilder.append("*/");
        return new BoundSql.BoundSqlObj(sqlBuilder + boundSql.getSqlString(), boundSql.getArgs());
    }
}
