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
package net.hasor.dbvisitor.dialect;

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