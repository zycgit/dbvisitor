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
package net.hasor.dbvisitor.faker;

import net.hasor.dbvisitor.faker.generator.SqlType;

import java.util.Arrays;

/**
 * 可执行的 SQL
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class BoundSql {

    private final SqlType  sqlType;
    private final String   sqlString;
    private final SqlArg[] paramArray;

    public BoundSql(SqlType sqlType, String sqlString) {
        this.sqlType = sqlType;
        this.sqlString = sqlString;
        this.paramArray = new SqlArg[0];
    }

    public BoundSql(SqlType sqlType, String sqlString, SqlArg[] paramArray) {
        this.sqlType = sqlType;
        this.sqlString = sqlString;
        this.paramArray = paramArray;
    }

    public String getSqlString() {
        return this.sqlString;
    }

    public SqlArg[] getArgs() {
        return this.paramArray;
    }

    @Override
    public String toString() {
        return "BoundSqlObj{'" + sqlString + '\'' + ", args=" + Arrays.toString(paramArray) + '}';
    }
}
