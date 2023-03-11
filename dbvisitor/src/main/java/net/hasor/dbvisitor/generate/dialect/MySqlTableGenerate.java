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
package net.hasor.dbvisitor.generate.dialect;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.JdbcUtils;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.mapping.def.*;
import net.hasor.dbvisitor.generate.SqlTableGenerate;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.*;
import java.time.chrono.JapaneseDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 针对 MySQL 的表结构生成器
 * <li>https://dev.mysql.com/doc/refman/5.7/en/data-types.html</li>
 * <li>https://dev.mysql.com/doc/refman/8.0/en/data-types.html</li>
 * @version : 2023-03-04
 * @author 赵永春 (zyc@hasor.net)
 */
public class MySqlTableGenerate extends SqlTableGenerate {
    public MySqlTableGenerate() {
        super(SqlDialectRegister.findOrCreate(JdbcUtils.MYSQL));
    }

    @Override
    protected void afterColum(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            TableMapping<?> tableMapping, ColumnMapping colMapping) {
        ColumnDescription description = colMapping.getDescription();
        String characterSet = description.getCharacterSet();
        String collation = description.getCollation();
        String comment = description.getComment();
        String other = description.getOther();

        if (StringUtils.isNotBlank(characterSet)) {
            scriptBuild.append(" CHARACTER SET '").append(characterSet).append("'");
        }
        if (StringUtils.isNotBlank(collation)) {
            scriptBuild.append(" COLLATE ").append(collation);
        }
        if (StringUtils.isNotBlank(comment)) {
            scriptBuild.append(" COMMENT '").append(comment.replace("'", "''")).append("'");
        }
        if (StringUtils.isNotBlank(other)) {
            scriptBuild.append(other);
        }
    }

    @Override
    protected void afterTable(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            TableMapping<?> tableMapping) {
        TableDescription description = tableMapping.getDescription();
        String characterSet = description.getCharacterSet();
        String collation = description.getCollation();
        String comment = description.getComment();
        String other = description.getOther();

        if (StringUtils.isNotBlank(characterSet)) {
            scriptBuild.append(" DEFAULT CHARSET = ").append(characterSet);
        }
        if (StringUtils.isNotBlank(collation)) {
            scriptBuild.append(" COLLATE = ").append(collation);
        }
        if (StringUtils.isNotBlank(comment)) {
            scriptBuild.append(" COMMENT '").append(comment.replace("'", "''")).append("'");
        }
        if (StringUtils.isNotBlank(other)) {
            scriptBuild.append(other);
        }
    }

    @Override
    protected boolean buildIndex(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            TableMapping<?> tableMapping, IndexDescription index) {
        String name = index.getName();
        boolean delimited = tableMapping.useDelimited();
        scriptBuild.append("KEY " + dialect.fmtName(delimited, name) + "(");

        List<String> ukColumns = index.getColumns();
        for (int i = 0; i < ukColumns.size(); i++) {
            String column = ukColumns.get(i);
            if (i > 0) {
                scriptBuild.append(", ");
            }
            scriptBuild.append(dialect.fmtName(delimited, column));
        }
        scriptBuild.append(")");
        return true;
    }

    private static final Map<Class<?>, String> javaTypeToJdbcTypeMap = new ConcurrentHashMap<>();

    static {
        // primitive and wrapper
        javaTypeToJdbcTypeMap.put(Boolean.class, "BOOLEAN");
        javaTypeToJdbcTypeMap.put(boolean.class, "BOOLEAN");
        javaTypeToJdbcTypeMap.put(Byte.class, "TINYINT");
        javaTypeToJdbcTypeMap.put(byte.class, "TINYINT");
        javaTypeToJdbcTypeMap.put(Short.class, "SMALLINT");
        javaTypeToJdbcTypeMap.put(short.class, "SMALLINT");
        javaTypeToJdbcTypeMap.put(Integer.class, "INT");
        javaTypeToJdbcTypeMap.put(int.class, "INT");
        javaTypeToJdbcTypeMap.put(Long.class, "BIGINT");
        javaTypeToJdbcTypeMap.put(long.class, "BIGINT");
        javaTypeToJdbcTypeMap.put(Float.class, "FLOAT");
        javaTypeToJdbcTypeMap.put(float.class, "FLOAT");
        javaTypeToJdbcTypeMap.put(Double.class, "DOUBLE");
        javaTypeToJdbcTypeMap.put(double.class, "DOUBLE");
        javaTypeToJdbcTypeMap.put(Character.class, "CHAR");
        javaTypeToJdbcTypeMap.put(char.class, "CHAR");
        // java time
        javaTypeToJdbcTypeMap.put(Date.class, "DATE");
        javaTypeToJdbcTypeMap.put(java.sql.Date.class, "DATE");
        javaTypeToJdbcTypeMap.put(java.sql.Timestamp.class, "DATETIME");
        javaTypeToJdbcTypeMap.put(java.sql.Time.class, "TIME");
        javaTypeToJdbcTypeMap.put(Instant.class, "DATETIME");
        javaTypeToJdbcTypeMap.put(LocalDateTime.class, "DATETIME");
        javaTypeToJdbcTypeMap.put(LocalDate.class, "DATE");
        javaTypeToJdbcTypeMap.put(LocalTime.class, "TIME");
        javaTypeToJdbcTypeMap.put(ZonedDateTime.class, "TIMESTAMP");
        javaTypeToJdbcTypeMap.put(JapaneseDate.class, "DATETIME");
        javaTypeToJdbcTypeMap.put(YearMonth.class, "MEDIUMINT");
        javaTypeToJdbcTypeMap.put(Year.class, "YEAR");
        javaTypeToJdbcTypeMap.put(Month.class, "TINYINT");
        javaTypeToJdbcTypeMap.put(OffsetDateTime.class, "DATETIME");
        javaTypeToJdbcTypeMap.put(OffsetTime.class, "TIME");
        // java extensions Types
        javaTypeToJdbcTypeMap.put(String.class, "VARCHAR");
        javaTypeToJdbcTypeMap.put(BigInteger.class, "BIGINT");
        javaTypeToJdbcTypeMap.put(BigDecimal.class, "DECIMAL");
        javaTypeToJdbcTypeMap.put(Reader.class, "TEXT");
        javaTypeToJdbcTypeMap.put(InputStream.class, "BLOB");
        javaTypeToJdbcTypeMap.put(URL.class, "VARCHAR");
        javaTypeToJdbcTypeMap.put(Byte[].class, "VARBINARY");
        javaTypeToJdbcTypeMap.put(byte[].class, "VARBINARY");
        // javaTypeToJdbcTypeMap.put(Object[].class, Types.ARRAY);
        // javaTypeToJdbcTypeMap.put(Object.class, Types.JAVA_OBJECT);
        // 枚举
    }

    @Override
    protected String typeBuild(Class<?> javaType, ColumnDescription description) {
        description = description == null ? EMPTY : description;
        String sqlType = description.getSqlType();
        String length = description.getLength();
        String precision = description.getPrecision();
        String scale = description.getScale();

        if (StringUtils.isNotBlank(sqlType)) {
            return sqlType;
        }

        sqlType = javaTypeToJdbcTypeMap.get(javaType);
        if (StringUtils.isBlank(sqlType)) {
            if (javaType.isEnum()) {
                sqlType = "VARCHAR";
                length = StringUtils.isNotBlank(length) ? length : String.valueOf(enumNameLengthHelper(javaType));
            } else {
                throw new UnsupportedOperationException(javaType + " Unsupported.");
            }
        }

        switch (sqlType) {
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INT":
            case "BIGINT":
                return sqlType + (StringUtils.isBlank(precision) ? "" : ("(" + precision + ")"));
            case "DOUBLE": {
                if (StringUtils.isNotBlank(precision) && StringUtils.isNotBlank(scale)) {
                    return "DOUBLE(" + precision + ", " + scale + ")";
                } else {
                    return "DOUBLE";
                }
            }
            case "FLOAT":
            case "DECIMAL": {
                if (StringUtils.isNotBlank(precision) && StringUtils.isNotBlank(scale)) {
                    return sqlType + "(" + precision + ", " + scale + ")";
                } else if (StringUtils.isNotBlank(precision)) {
                    return sqlType + "(" + precision + ")";
                } else {
                    return sqlType;
                }
            }
            case "CHAR":
                return "CHAR" + (StringUtils.isBlank(length) ? "" : ("(" + length + ")"));
            case "VARCHAR":
                if (StringUtils.equalsIgnoreCase(length, "small")) {
                    return "TINYTEXT";
                } else if (StringUtils.equalsIgnoreCase(length, "large")) {
                    return "LONGTEXT";
                } else {
                    return StringUtils.isBlank(length) ? "TEXT" : ("VARCHAR(" + length + ")");
                }
            case "TEXT":
                if (StringUtils.equalsIgnoreCase(length, "small")) {
                    return "TINYTEXT";
                } else if (StringUtils.equalsIgnoreCase(length, "large")) {
                    return "LONGTEXT";
                } else {
                    return "TEXT";
                }
            case "TIME":
            case "TIMESTAMP":
            case "DATETIME":
                return sqlType + (StringUtils.isBlank(precision) ? "" : ("(" + precision + ")"));
            case "VARBINARY": {
                if (StringUtils.equalsIgnoreCase(length, "small")) {
                    return "TINYBLOB";
                } else if (StringUtils.equalsIgnoreCase(length, "large")) {
                    return "LONGBLOB";
                } else {
                    return StringUtils.isBlank(length) ? "BLOB" : ("VARBINARY(" + length + ")");
                }
            }
            case "BLOB": {
                if (StringUtils.equalsIgnoreCase(length, "small")) {
                    return "TINYBLOB";
                } else if (StringUtils.equalsIgnoreCase(length, "large")) {
                    return "LONGBLOB";
                } else {
                    return "BLOB" + (StringUtils.isBlank(length) ? "" : ("BLOB(" + length + ")"));
                }
            }
            default:
                return sqlType;
        }
    }

    @Override
    protected String buildDefault(String sqlType, String defaultValue) {
        boolean isBoolean = StringUtils.startsWithIgnoreCase(sqlType, "BOOLEAN");
        boolean isNumber = StringUtils.startsWithIgnoreCase(sqlType, "TINYINT") //
                || StringUtils.startsWithIgnoreCase(sqlType, "SMALLINT") //
                || StringUtils.startsWithIgnoreCase(sqlType, "MEDIUMINT") //
                || StringUtils.startsWithIgnoreCase(sqlType, "INT") //
                || StringUtils.startsWithIgnoreCase(sqlType, "BIGINT")//
                || StringUtils.startsWithIgnoreCase(sqlType, "DOUBLE")//
                || StringUtils.startsWithIgnoreCase(sqlType, "FLOAT")//
                || StringUtils.startsWithIgnoreCase(sqlType, "DECIMAL")//
                || StringUtils.startsWithIgnoreCase(sqlType, "YEAR");
        boolean isChar = StringUtils.startsWithIgnoreCase(sqlType, "CHAR") //
                || StringUtils.startsWithIgnoreCase(sqlType, "VARCHAR") //
                || StringUtils.startsWithIgnoreCase(sqlType, "TEXT");
        boolean isTime = StringUtils.startsWithIgnoreCase(sqlType, "DATE")//
                || StringUtils.startsWithIgnoreCase(sqlType, "TIME")//
                || StringUtils.startsWithIgnoreCase(sqlType, "TIMESTAMP")//
                || StringUtils.startsWithIgnoreCase(sqlType, "DATETIME");
        boolean isBlob = StringUtils.startsWithIgnoreCase(sqlType, "VARBINARY")//
                || StringUtils.startsWithIgnoreCase(sqlType, "BLOB");

        if (isChar || isTime) {
            return "'" + defaultValue.replace("'", "''") + "'";
        } else {
            return defaultValue;
        }
    }
}