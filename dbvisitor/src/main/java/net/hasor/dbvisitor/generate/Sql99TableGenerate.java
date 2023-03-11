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
package net.hasor.dbvisitor.generate;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.mapping.def.ColumnDescription;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.Types;

/**
 * 基于 SQL-99 语法的建表语句生成器（SQL-99 规范 https://ronsavage.github.io/SQL/sql-99.bnf.html）
 * - 扩展自 SQL-92，新增了 CLOB、NCLOB、BLOB、BOOLEAN 四个类型（是否支持依据具体数据库而定）
 * @version : 2023-03-04
 * @author 赵永春 (zyc@hasor.net)
 */
public class Sql99TableGenerate extends Sql92TableGenerate {
    public Sql99TableGenerate(SqlDialect dialect) {
        super(dialect);
    }

    @Override
    protected String typeBuild(Class<?> javaType, ColumnDescription description) {
        int jdbcType = javaType.isEnum() ? Types.VARCHAR : TypeHandlerRegistry.toSqlType(javaType);
        switch (jdbcType) {
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.LONGVARCHAR:
            case Types.CLOB:
                return "CLOB";
            case Types.LONGNVARCHAR:
            case Types.NCLOB:
                return "NCLOB";
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return "BLOB";
            default:
                return super.typeBuild(javaType, description);
        }
    }

    @Override
    protected String buildDefault(String sqlType, String defaultValue) {
        boolean isBoolean = StringUtils.startsWithIgnoreCase(sqlType, "BOOLEAN");
        boolean isChar = StringUtils.startsWithIgnoreCase(sqlType, "CLOB") //
                || StringUtils.startsWithIgnoreCase(sqlType, "NCLOB");
        boolean isBlob = StringUtils.startsWithIgnoreCase(sqlType, "BLOB");

        if (isChar) {
            return "'" + defaultValue.replace("'", "''") + "'";
        } else if (isBoolean || isBlob) {
            return defaultValue;
        } else {
            return super.buildDefault(sqlType, defaultValue);
        }
    }
}