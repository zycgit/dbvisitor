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
package net.hasor.dbvisitor.lambda.segment;
import net.hasor.dbvisitor.dialect.SqlDialect;

/**
 * 排序相关的关键字。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-11-02
 */
public enum OrderByKeyword implements Segment {
    ORDER_DEFAULT(""),
    ASC("ASC"),
    DESC("DESC");

    private final String sqlString;

    OrderByKeyword(String sqlString) {
        this.sqlString = sqlString;
    }

    @Override
    public String getSqlSegment(SqlDialect dialect) {
        return this.sqlString;
    }
}
