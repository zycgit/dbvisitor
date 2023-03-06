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
package net.hasor.dbvisitor.mapping.generate;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.mapping.def.ColumnDescription;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.Types;

/**
 * 基于 SQL-92 语法的建表语句生成器（SQL-92 规范 https://ronsavage.github.io/SQL/sql-92.bnf.html）
 * - 由于各数据库厂商对于 SQL-92 支持性不同，已知 PG 可以完整支持。已知下列类型在 MySQL\ORACLE\SQL SERVER 均只支持部分类型
 * @version : 2023-03-04
 * @author 赵永春 (zyc@hasor.net)
 */
public class Sql92TableGenerate extends SqlTableGenerate {
    @Override
    protected String typeBuild(Class<?> javaType, ColumnDescription description) {
        description = description == null ? EMPTY : description;
        String length = description.getLength();
        String precision = description.getPrecision();
        String scale = description.getScale();

        int jdbcType = TypeHandlerRegistry.toSqlType(javaType);
        switch (jdbcType) {
            case Types.BIT:
                return "BIT" + (StringUtils.isBlank(length) ? "" : (" VARYING(" + length + ")"));
            case Types.BOOLEAN:
            case Types.TINYINT:
            case Types.SMALLINT:
                return "SMALLINT";
            case Types.INTEGER:
            case Types.BIGINT:
                return "INTEGER";
            case Types.FLOAT:
                return "FLOAT" + (StringUtils.isBlank(precision) ? "" : ("(" + precision + ")"));
            case Types.REAL:
                return "REAL";
            case Types.DOUBLE:
                return "DOUBLE PRECISION";
            case Types.NUMERIC:
            case Types.DECIMAL: {
                if (StringUtils.isNotBlank(precision) && StringUtils.isNotBlank(scale)) {
                    return "DECIMAL(" + precision + ", " + scale + ")";
                } else if (StringUtils.isNotBlank(precision)) {
                    return "DECIMAL(" + precision + ")";
                } else {
                    return "DECIMAL";
                }
            }
            case Types.CHAR:
                return "CHAR" + (StringUtils.isBlank(length) ? "" : ("(" + length + ")"));
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
            case Types.DISTINCT:
            case Types.DATALINK:
            case Types.SQLXML:
                return "VARCHAR" + (StringUtils.isBlank(length) ? "" : ("(" + length + ")"));
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME" + (StringUtils.isBlank(precision) ? "" : ("(" + precision + ")"));
            case Types.TIME_WITH_TIMEZONE:
                return "TIME" + (StringUtils.isBlank(precision) ? "" : ("(" + precision + ")")) + " WITH TIME ZONE";
            case Types.TIMESTAMP:
                return "TIMESTAMP" + (StringUtils.isBlank(precision) ? "" : ("(" + precision + ")"));
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "TIMESTAMP" + (StringUtils.isBlank(precision) ? "" : ("(" + precision + ")")) + " WITH TIME ZONE";
            case Types.NCHAR:
                return "NCHAR" + (StringUtils.isBlank(length) ? "" : ("(" + length + ")"));
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.NCLOB:
                return "NATIONAL CHAR VARYING" + (StringUtils.isBlank(length) ? "" : ("(" + length + ")"));
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
            case Types.NULL:
            case Types.OTHER:
            case Types.JAVA_OBJECT:
            case Types.STRUCT:
            case Types.ARRAY:
            case Types.REF:
            case Types.ROWID:
            case Types.REF_CURSOR:
            default:
                throw new UnsupportedOperationException("[" + jdbcType + "]" + javaType + " Unsupported.");
        }
    }

    @Override
    protected String buildDefault(String sqlType, String defaultValue) {
        boolean isNumber = StringUtils.startsWithIgnoreCase(sqlType, "SMALLINT") //
                || StringUtils.startsWithIgnoreCase(sqlType, "INTEGER") //
                || StringUtils.startsWithIgnoreCase(sqlType, "FLOAT") //
                || StringUtils.startsWithIgnoreCase(sqlType, "DOUBLE PRECISION") //
                || StringUtils.startsWithIgnoreCase(sqlType, "DECIMAL");

        if (isNumber) {
            return defaultValue;
        } else {
            return "'" + defaultValue.replace("'", "''") + "'";
        }
    }
}