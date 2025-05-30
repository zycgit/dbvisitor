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
 * SQL 查询相关的关键字。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public enum SqlKeyword implements Segment {
    AND("AND"),                 //
    OR("OR"),                   //
    IN("IN"),                   //
    NOT("NOT"),                 //
    LIKE("LIKE"),               //
    EQ("="),                    //
    NE("<>"),                   //
    GT(">"),                    //
    GE(">="),                   //
    LT("<"),                    //
    LE("<="),                   //
    IS("IS"),                   //
    NULL("NULL"),               //
    BETWEEN("BETWEEN"),         //

    GROUP_BY("GROUP BY"),       //
    HAVING("HAVING"),           //
    ORDER_BY("ORDER BY"),       //
    //ORDER_BY_DEFAULT(""),       //
    //ORDER_BY_ASC("ASC"),        //
    //ORDER_BY_DESC("DESC"),      //

    INSERT("INSERT"),           //
    UPDATE("UPDATE"),           //
    SET("SET"),                 //
    INTO("INTO"),               //
    VALUES("VALUES"),           //
    EXISTS("EXISTS"),           //
    SELECT("SELECT"),           //
    DELETE("DELETE"),           //
    FROM("FROM"),               //
    WHERE("WHERE"),             //
    EMPTY(""),                  //
    LEFT("("),                  //
    RIGHT(")")                  //
    ;

    private final String sqlString;

    SqlKeyword(String sqlString) {
        this.sqlString = sqlString;
    }

    public String getSqlString() {
        return this.sqlString;
    }

    @Override
    public String getSqlSegment(SqlDialect dialect) {
        return this.sqlString;
    }
}
