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
package net.hasor.dbvisitor.faker.provider.mysql.meta;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.meta.JdbcFetchMeta;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.extractor.RowMapperResultSetExtractor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于 JDBC 接口的元信息获取
 * @version : 2020-04-23
 * @author 赵永春 (zyc@hasor.net)
 */
public class MySqlFetchMeta implements JdbcFetchMeta {
    private static String SQL_PKUK = "select C.TABLE_SCHEMA,C.TABLE_NAME,C.COLUMN_NAME,T.CONSTRAINT_TYPE " //
            + "from INFORMATION_SCHEMA.KEY_COLUMN_USAGE C " //
            + "left join INFORMATION_SCHEMA.TABLE_CONSTRAINTS T " //
            + "on C.TABLE_SCHEMA = T.TABLE_SCHEMA and C.TABLE_NAME = T.TABLE_NAME and C.CONSTRAINT_CATALOG = T.CONSTRAINT_CATALOG and C.CONSTRAINT_SCHEMA = T.CONSTRAINT_SCHEMA and C.CONSTRAINT_NAME = T.CONSTRAINT_NAME " //
            + "where C.TABLE_SCHEMA = ? and C.TABLE_NAME = ? and T.CONSTRAINT_TYPE IN ('PRIMARY KEY','UNIQUE') " //
            + "order by C.ORDINAL_POSITION asc";

    private static String SQL_COLUMN = "select TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,IS_NULLABLE,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH,CHARACTER_OCTET_LENGTH,NUMERIC_SCALE,NUMERIC_PRECISION,DATETIME_PRECISION,COLUMN_TYPE,COLUMN_DEFAULT,COLUMN_COMMENT,ORDINAL_POSITION,EXTRA "//
            + "from INFORMATION_SCHEMA.COLUMNS " //
            + "where TABLE_SCHEMA = ? and TABLE_NAME = ?" //
            + "order by ORDINAL_POSITION asc";

    public List<JdbcColumn> getColumns(Connection conn, String catalog, String schemaName, String table) throws SQLException {
        JdbcTemplate jdbc = new JdbcTemplate(conn);

        // isMaria
        boolean isMaria = this.isMaria(jdbc.queryForString("select version()"));

        // PK and UK
        List<String> pkColumn = new ArrayList<>();
        List<String> ukColumn = new ArrayList<>();
        List<Map<String, Object>> result = jdbc.queryForList(SQL_PKUK, new Object[] { catalog, table });
        result.forEach(record -> {
            String constraintType = (String) record.get("CONSTRAINT_TYPE");
            if (StringUtils.equals("PRIMARY KEY", constraintType)) {
                pkColumn.add((String) record.get("COLUMN_NAME"));
            } else if (StringUtils.equals("UNIQUE", constraintType)) {
                ukColumn.add((String) record.get("COLUMN_NAME"));
            }
        });

        // columns
        try (PreparedStatement ps = conn.prepareStatement(SQL_COLUMN)) {
            ps.setString(1, catalog);
            ps.setString(2, table);

            try (ResultSet resultSet = ps.executeQuery()) {
                return new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                    return convertColumn(rs, isMaria, pkColumn, ukColumn);
                }).extractData(resultSet);
            }
        }
    }

    private JdbcColumn convertColumn(ResultSet rs, boolean isMaria, List<String> pkColumn, List<String> ukColumn) throws SQLException {
        JdbcColumn jdbcColumn = new JdbcColumn();
        jdbcColumn.setTableCatalog(rs.getString("TABLE_SCHEMA"));
        jdbcColumn.setTableSchema(null);
        jdbcColumn.setTableName(rs.getString("TABLE_NAME"));
        jdbcColumn.setColumnName(rs.getString("COLUMN_NAME"));

        String isNullable = rs.getString("IS_NULLABLE");
        if ("YES".equals(isNullable)) {
            jdbcColumn.setNullable(true);
        } else if ("NO".equals(isNullable)) {
            jdbcColumn.setNullable(false);
        } else {
            jdbcColumn.setNullable(null);
        }

        MySqlTypes dataTypeEnum = safeToMySqlTypes(rs.getString("DATA_TYPE"));
        String dataType = dataTypeEnum.getCodeKey();
        String columnType = rs.getString("COLUMN_TYPE");

        if (columnType.contains(" unsigned") || columnType.contains(" zerofill")) {
            dataType = dataType + " unsigned";
        }

        jdbcColumn.setColumnType(dataType.toLowerCase());
        jdbcColumn.setJdbcType(dataTypeEnum.getJdbcType());
        jdbcColumn.setComment(rs.getString("COLUMN_COMMENT"));

        String extra = rs.getString("EXTRA");
        if (StringUtils.isNotBlank(extra)) {
            if (extra.equalsIgnoreCase("auto_increment")) {
                jdbcColumn.setAutoincrement(true);
            }

            if (extra.contains("VIRTUAL GENERATED")) {
                jdbcColumn.setGeneratedColumn(true);
            }
        }

        long characterOctetLength = rs.getLong("CHARACTER_OCTET_LENGTH");
        long characterMaximumLength = rs.getLong("CHARACTER_MAXIMUM_LENGTH");
        Integer datetimePrecision = rs.getInt("DATETIME_PRECISION");
        Integer numericPrecision = rs.getInt("NUMERIC_PRECISION");
        Integer numericScale = rs.getInt("NUMERIC_SCALE");
        switch (dataTypeEnum) {
            case TIMESTAMP:
            case DATE:
            case TIME:
            case DATETIME:
                jdbcColumn.setColumnSize(datetimePrecision);
                break;
            case BIT:
                jdbcColumn.setColumnSize(numericPrecision);
                break;
            case TINYINT:
            case SMALLINT:
            case MEDIUMINT:
            case INT:
            case BIGINT:
            case FLOAT:
            case DOUBLE:
            case DECIMAL:
                jdbcColumn.setColumnSize(numericPrecision);
                jdbcColumn.setDecimalDigits(numericScale);
                break;
            default:
                jdbcColumn.setColumnSize(characterMaximumLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) characterMaximumLength);
                jdbcColumn.setCharOctetLength(characterOctetLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) characterOctetLength);
                break;
        }

        String dataDefault = rs.getString("COLUMN_DEFAULT");
        if (dataDefault != null) {
            if (isMaria) {
                jdbcColumn.setHasDefaultValue(parseMariaDefault(dataDefault));
            } else {
                jdbcColumn.setHasDefaultValue(true);
            }
        }
        jdbcColumn.setIndex(tryWasNull(rs.getInt("ORDINAL_POSITION"), rs));

        jdbcColumn.setPrimaryKey(pkColumn.contains(jdbcColumn.getColumnName()));
        jdbcColumn.setUniqueKey(ukColumn.contains(jdbcColumn.getColumnName()));
        return jdbcColumn;
    }

    protected MySqlTypes safeToMySqlTypes(Object obj) {
        String dat = (obj == null) ? null : obj.toString();
        for (MySqlTypes type : MySqlTypes.values()) {
            if (StringUtils.equalsIgnoreCase(type.getCodeKey(), dat)) {
                return type;
            }
        }
        return null;
    }

    protected Integer tryWasNull(int value, ResultSet record) throws SQLException {
        if (record.wasNull()) {
            return null;
        } else {
            return value;
        }
    }

    // -------------------------------------------------------------------------------------------
    //         MariaDB
    // -------------------------------------------------------------------------------------------

    private Boolean mariaTag = null;

    protected boolean isMaria(String dbVersion) {
        if (mariaTag == null) {
            this.mariaTag = StringUtils.contains(dbVersion.toUpperCase(), "MARIADB");
        }
        return mariaTag;
    }

    protected boolean parseMariaDefault(String dataDefault) {
        return dataDefault != null && !dataDefault.equals("NULL");
    }
}
