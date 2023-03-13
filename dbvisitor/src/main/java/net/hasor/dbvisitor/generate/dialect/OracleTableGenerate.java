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
import net.hasor.dbvisitor.generate.GenerateContext;
import net.hasor.dbvisitor.generate.SqlTableGenerate;
import net.hasor.dbvisitor.mapping.def.*;

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
public class OracleTableGenerate extends SqlTableGenerate {
    public OracleTableGenerate() {
        super(SqlDialectRegister.findOrCreate(JdbcUtils.ORACLE));
    }

    @Override
    protected void afterColum(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            GenerateContext context, TableMapping<?> tableMapping, ColumnMapping colMapping) {
        ColumnDescription description = colMapping.getDescription();
        boolean delimited = tableMapping.useDelimited();

        String characterSet = description.getCharacterSet();
        String collation = description.getCollation();
        if (StringUtils.isNotBlank(characterSet)) {
            scriptBuild.append(" CHARACTER SET ").append(characterSet);
        }
        if (StringUtils.isNotBlank(collation)) {
            scriptBuild.append(" COLLATE ").append(collation);
        }

        String other = description.getOther();
        if (StringUtils.isNotBlank(other)) {
            scriptBuild.append(" ").append(other);
        }

        String comment = description.getComment();
        if (StringUtils.isNotBlank(comment)) {
            String catalog = tableMapping.getCatalog();
            String schema = tableMapping.getSchema();
            String table = tableMapping.getTable();
            String column = colMapping.getColumn();

            StringBuilder sqlBuild = new StringBuilder();
            sqlBuild.append("COMMENT ON COLUMN ");
            sqlBuild.append(this.tableName(delimited, context, catalog, schema, table));
            sqlBuild.append(".");
            sqlBuild.append(this.fmtName(delimited, context, column));
            sqlBuild.append(" IS '" + comment.replace("'", "''") + "'");
            afterScripts.add(sqlBuild.toString());
        }
    }

    @Override
    protected void afterTable(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            GenerateContext context, TableMapping<?> tableMapping) {
        TableDescription description = tableMapping.getDescription();
        boolean delimited = tableMapping.useDelimited();

        String other = description.getOther();
        if (StringUtils.isNotBlank(other)) {
            scriptBuild.append(" ").append(other);
        }

        String comment = description.getComment();
        if (StringUtils.isNotBlank(comment)) {
            String catalog = tableMapping.getCatalog();
            String schema = tableMapping.getSchema();
            String table = tableMapping.getTable();

            StringBuilder sqlBuild = new StringBuilder();
            sqlBuild.append("COMMENT ON TABLE ");
            sqlBuild.append(this.tableName(delimited, context, catalog, schema, table));
            sqlBuild.append(" IS '" + comment.replace("'", "''") + "'");
            afterScripts.add(sqlBuild.toString());
        }
    }

    @Override
    protected boolean buildIndex(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            GenerateContext context, TableMapping<?> tableMapping, IndexDescription index) {
        String catalog = tableMapping.getCatalog();
        String schema = tableMapping.getSchema();
        String table = tableMapping.getTable();
        boolean delimited = tableMapping.useDelimited();
        String indexName = index.getName();
        List<String> columnList = index.getColumns();

        StringBuilder sqlBuild = new StringBuilder();
        sqlBuild.append("CREATE INDEX ");
        sqlBuild.append(this.tableName(delimited, context, catalog, schema, indexName));
        sqlBuild.append(" ON ");
        sqlBuild.append(this.tableName(delimited, context, catalog, schema, table));
        sqlBuild.append("(");
        for (int i = 0; i < columnList.size(); i++) {
            String column = columnList.get(i);
            if (i > 0) {
                sqlBuild.append(", ");
            }
            sqlBuild.append(this.fmtName(delimited, context, column));
        }
        sqlBuild.append(")");
        String other = index.getOther();
        if (StringUtils.isNotBlank(other)) {
            scriptBuild.append(" ").append(other);
        }

        afterScripts.add(sqlBuild.toString());
        return true;
    }

    private static final Map<Class<?>, String> javaTypeToJdbcTypeMap = new ConcurrentHashMap<>();

    static {
        // primitive and wrapper
        javaTypeToJdbcTypeMap.put(Boolean.class, "SMALLINT");
        javaTypeToJdbcTypeMap.put(boolean.class, "SMALLINT");
        javaTypeToJdbcTypeMap.put(Byte.class, "SMALLINT");
        javaTypeToJdbcTypeMap.put(byte.class, "SMALLINT");
        javaTypeToJdbcTypeMap.put(Short.class, "SMALLINT");
        javaTypeToJdbcTypeMap.put(short.class, "SMALLINT");
        javaTypeToJdbcTypeMap.put(Integer.class, "INTEGER");
        javaTypeToJdbcTypeMap.put(int.class, "INTEGER");
        javaTypeToJdbcTypeMap.put(Long.class, "NUMBER");
        javaTypeToJdbcTypeMap.put(long.class, "NUMBER");
        javaTypeToJdbcTypeMap.put(Float.class, "FLOAT");
        javaTypeToJdbcTypeMap.put(float.class, "FLOAT");

        javaTypeToJdbcTypeMap.put(Double.class, "DOUBLE PRECISION");
        javaTypeToJdbcTypeMap.put(double.class, "DOUBLE PRECISION");
        javaTypeToJdbcTypeMap.put(Character.class, "CHAR");
        javaTypeToJdbcTypeMap.put(char.class, "CHAR");
        // java time
        javaTypeToJdbcTypeMap.put(Date.class, "TIMESTAMP");
        javaTypeToJdbcTypeMap.put(java.sql.Timestamp.class, "TIMESTAMP");
        javaTypeToJdbcTypeMap.put(java.sql.Date.class, "DATE");
        javaTypeToJdbcTypeMap.put(java.sql.Time.class, "TIMESTAMP");
        javaTypeToJdbcTypeMap.put(LocalDate.class, "DATE");
        javaTypeToJdbcTypeMap.put(LocalTime.class, "TIMESTAMP");
        javaTypeToJdbcTypeMap.put(LocalDateTime.class, "TIMESTAMP");
        javaTypeToJdbcTypeMap.put(ZonedDateTime.class, "TIMESTAMP WITH TIME ZONE");
        javaTypeToJdbcTypeMap.put(OffsetTime.class, "TIMESTAMP WITH TIME ZONE");
        javaTypeToJdbcTypeMap.put(OffsetDateTime.class, "TIMESTAMP WITH TIME ZONE");
        javaTypeToJdbcTypeMap.put(Instant.class, "TIMESTAMP");
        javaTypeToJdbcTypeMap.put(JapaneseDate.class, "TIMESTAMP");
        javaTypeToJdbcTypeMap.put(YearMonth.class, "INTEGER");
        javaTypeToJdbcTypeMap.put(Year.class, "SMALLINT");
        javaTypeToJdbcTypeMap.put(Month.class, "SMALLINT");
        // java extensions Types
        javaTypeToJdbcTypeMap.put(String.class, "VARCHAR2");
        javaTypeToJdbcTypeMap.put(BigInteger.class, "NUMBER");
        javaTypeToJdbcTypeMap.put(BigDecimal.class, "NUMBER");
        javaTypeToJdbcTypeMap.put(Reader.class, "CLOB");
        javaTypeToJdbcTypeMap.put(InputStream.class, "BLOB");
        javaTypeToJdbcTypeMap.put(URL.class, "VARCHAR");
        javaTypeToJdbcTypeMap.put(Byte[].class, "RAW");
        javaTypeToJdbcTypeMap.put(byte[].class, "RAW");
        // javaTypeToJdbcTypeMap.put(Object[].class, Types.ARRAY);
        // javaTypeToJdbcTypeMap.put(Object.class, Types.JAVA_OBJECT);
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
            throw new UnsupportedOperationException(javaType + " Unsupported.");
        }

        switch (sqlType) {
            case "FLOAT":
                return "FLOAT" + (StringUtils.isBlank(precision) ? "" : ("(" + precision + ")"));
            case "NUMBER":
                if (StringUtils.isNotBlank(precision) && StringUtils.isNotBlank(scale)) {
                    return sqlType + "(" + precision + ", " + scale + ")";
                } else if (StringUtils.isBlank(precision) && StringUtils.isNotBlank(scale)) {
                    return sqlType + "(*, " + scale + ")";
                } else if (StringUtils.isNotBlank(precision)) {
                    return sqlType + "(" + precision + ")";
                } else {
                    return sqlType;
                }
            case "CHAR":
                return StringUtils.isBlank(length) ? "CHAR" : ("CHAR(" + length + ")");
            case "VARCHAR":
            case "VARCHAR2":
                return StringUtils.isBlank(length) ? "CLOB" : (sqlType + "(" + length + ")");
            case "RAW":
                return StringUtils.isBlank(length) ? "RAW(2000)" : ("RAW(" + length + ")");
            case "TIMESTAMP":
                return StringUtils.isBlank(precision) ? "TIMESTAMP" : ("TIMESTAMP(" + precision + ")");
            case "TIMESTAMP WITH TIME ZONE":
                return (StringUtils.isBlank(precision) ? "TIMESTAMP" : ("TIMESTAMP(" + precision + ")")) + " WITH TIME ZONE";
            default:
                return sqlType;
        }
    }

    @Override
    protected String buildDefault(String sqlType, String defaultValue) {
        boolean isNumber = StringUtils.startsWithIgnoreCase(sqlType, "FLOAT") //
                || StringUtils.startsWithIgnoreCase(sqlType, "NUMBER") //
                || StringUtils.startsWithIgnoreCase(sqlType, "SMALLINT") //
                || StringUtils.startsWithIgnoreCase(sqlType, "INTEGER") //
                || StringUtils.startsWithIgnoreCase(sqlType, "DOUBLE PRECISION");

        if (isNumber) {
            return defaultValue;
        } else {
            return "'" + defaultValue.replace("'", "''") + "'";
        }
    }
}