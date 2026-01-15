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
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;

/**
 * DB2 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class Db2Dialect extends AbstractSqlDialect implements PageSqlDialect {
    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/db2.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "\"";
    }

    @Override
    public SqlCommandBuilder newBuilder() {
        return new Db2Dialect();
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS ROW_ID FROM ( ");
        sb.append(boundSql.getSqlString());
        sb.append(" ) AS TMP_PAGE) TMP_PAGE WHERE ROW_ID BETWEEN ? AND ?");

        Object[] paramArray = boundSql.getArgs();
        Object[] destArgs = new Object[paramArray.length + 2];
        System.arraycopy(paramArray, 0, destArgs, 0, paramArray.length);
        destArgs[paramArray.length] = start;
        destArgs[paramArray.length + 1] = limit;
        return new BoundSql.BoundSqlObj(sb.toString(), destArgs);
    }
}