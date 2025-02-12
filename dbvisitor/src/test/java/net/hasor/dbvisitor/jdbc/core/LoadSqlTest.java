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
package net.hasor.dbvisitor.jdbc.core;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static net.hasor.test.utils.DsUtils.MYSQL_SCHEMA_NAME;

/***
 * execute 系列方法测试
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class LoadSqlTest extends AbstractDbTest {
    public boolean hasTable(JdbcTemplate jdbcTemplate, String catalog, String schemaName, String table) throws SQLException {
        return jdbcTemplate.execute((ConnectionCallback<Boolean>) con -> {
            DatabaseMetaData metaData = con.getMetaData();
            try (ResultSet resultSet = metaData.getTables(catalog, schemaName, table, null)) {
                List<String> jdbcTables = new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                    return rs.getString("TABLE_NAME");
                }).extractData(resultSet);
                return jdbcTables.contains(table);
            }
        });
    }

    @Test
    public void loadSQL_1() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info")) {
                jdbcTemplate.executeUpdate("drop table user_info");
            }

            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
            jdbcTemplate.loadSQL("/dbvisitor_coverage/user_info_for_mysql.sql");
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
        }
    }

    @Test
    public void loadSQL_2() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info")) {
                jdbcTemplate.executeUpdate("drop table user_info");
            }

            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
            InputStream asStream = ResourcesUtils.getResourceAsStream("/dbvisitor_coverage/user_info_for_mysql.sql");
            if (asStream == null) {
                assert false;
            }
            jdbcTemplate.loadSQL(new InputStreamReader(asStream));
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
        }
    }

    @Test
    public void loadSQL_3() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info")) {
                jdbcTemplate.executeUpdate("drop table user_info");
            }

            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
            jdbcTemplate.loadSQL(StandardCharsets.UTF_8, "/dbvisitor_coverage/user_info_for_mysql.sql");
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
        }
    }

    @Test
    public void loadSplitSQL_1() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info")) {
                jdbcTemplate.executeUpdate("drop table user_info");
            }

            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
            jdbcTemplate.loadSplitSQL(";", "/dbvisitor_coverage/user_info_for_mysql.sql");
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
        }
    }

    @Test
    public void loadSplitSQL_2() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info")) {
                jdbcTemplate.executeUpdate("drop table user_info");
            }

            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
            jdbcTemplate.loadSplitSQL(";", StandardCharsets.UTF_8, "/dbvisitor_coverage/user_info_for_mysql.sql");
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
        }
    }

    @Test
    public void loadSplitSQL_3() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info")) {
                jdbcTemplate.executeUpdate("drop table user_info");
            }

            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
            InputStream asStream = ResourcesUtils.getResourceAsStream("/dbvisitor_coverage/user_info_for_mysql.sql");
            if (asStream == null) {
                assert false;
            }
            jdbcTemplate.loadSplitSQL(";", new InputStreamReader(asStream));
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "user_info");
        }
    }

    @Test
    public void badTest_1() {
        try {
            new JdbcTemplate().loadSplitSQL(";", StandardCharsets.UTF_8, "abc");
        } catch (Exception e) {
            assert e.getMessage().equals("can't find resource 'abc'");
        }
    }

    @Test
    public void badTest_2() {
        try {
            new JdbcTemplate().executeBatch(new String[0]);
        } catch (NullPointerException | SQLException e) {
            assert e.getMessage().equals("SQL array must not be empty");
        }
    }
}