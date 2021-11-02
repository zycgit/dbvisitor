/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.db.dialect.provider;
import net.hasor.db.dialect.BoundSql;
import net.hasor.db.dialect.InsertSqlDialect;
import net.hasor.db.dialect.PageSqlDialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MySQL 的 SqlDialect 实现
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class MySqlDialect extends AbstractDialect implements PageSqlDialect, InsertSqlDialect {
    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/mysql.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "`";
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, int start, int limit) {
        StringBuilder sqlBuilder = new StringBuilder(boundSql.getSqlString());
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));

        if (start <= 0) {
            sqlBuilder.append(" LIMIT ?");
            paramArrays.add(limit);
        } else {
            sqlBuilder.append(" LIMIT ?, ?");
            paramArrays.add(start);
            paramArrays.add(limit);
        }

        return new BoundSql.BoundSqlObj(sqlBuilder.toString(), paramArrays.toArray());
    }

    @Override
    public boolean supportInsertInto(List<String> primaryKey, List<String> columns) {
        return true;
    }

    @Override
    public String insertWithInto(boolean useQualifier, String schema, String table, List<String> primaryKey, List<String> columns) {
        return buildSql("INSERT INTO ", useQualifier, schema, table, columns, "");
    }

    @Override
    public boolean supportInsertIgnore(List<String> primaryKey, List<String> columns) {
        return true;
    }

    @Override
    public String insertWithIgnore(boolean useQualifier, String schema, String table, List<String> primaryKey, List<String> columns) {
        return buildSql("INSERT IGNORE ", useQualifier, schema, table, columns, "");
    }

    @Override
    public boolean supportUpsert(List<String> primaryKey, List<String> columns) {
        return true;
    }

    @Override
    public String insertWithUpsert(boolean useQualifier, String schema, String table, List<String> primaryKey, List<String> columns) {
        return buildSql("INSERT INTO ", useQualifier, schema, table, columns, " ON DUPLICATE KEY UPDATE");
    }

    protected String buildSql(String markString, boolean useQualifier, String schema, String table, List<String> columns, String appendSql) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(markString);
        strBuilder.append(tableName(useQualifier, schema, table));
        strBuilder.append(" ");
        strBuilder.append("(");

        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                strBuilder.append(", ");
                argBuilder.append(", ");
            }
            strBuilder.append(columnName(useQualifier, schema, table, columns.get(i)));
            argBuilder.append("?");
        }

        strBuilder.append(") VALUES (");
        strBuilder.append(argBuilder);
        strBuilder.append(")");
        strBuilder.append(appendSql);
        return strBuilder.toString();
    }

}
