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
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.PageSqlDialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PostgreSQL 对象名有大小写敏感不敏感的问题
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class PostgreSqlDialect extends AbstractDialect implements PageSqlDialect {
    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/postgresql.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "\"";
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sqlBuilder = new StringBuilder(boundSql.getSqlString());
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));

        if (limit > 0) {
            sqlBuilder.append(" LIMIT ?");
            paramArrays.add(limit);
        }
        if (start > 0) {
            sqlBuilder.append(" OFFSET ?");
            paramArrays.add(start);
        }

        return new BoundSql.BoundSqlObj(sqlBuilder.toString(), paramArrays.toArray());
    }

    public String randomQuery(boolean useQualifier, String catalog, String schema, String table, List<String> selectColumns, int recordSize) {
        String tableName = this.tableName(useQualifier, catalog, schema, table);
        StringBuilder select = new StringBuilder();

        if (selectColumns == null || selectColumns.isEmpty()) {
            select.append("*");
        } else {
            for (String col : selectColumns) {
                if (select.length() > 0) {
                    select.append(", ");
                }
                select.append(this.fmtName(useQualifier, col));
            }
        }

        return "select " + select + " from " + tableName + " order by random() limit " + recordSize;
    }
}
