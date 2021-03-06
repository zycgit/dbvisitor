/*
 * Copyright 2008-2009 the original author or authors.
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
import net.hasor.test.db.AbstractDbTest;
import net.hasor.test.db.utils.DsUtils;
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

import static net.hasor.test.db.utils.DsUtils.MYSQL_SCHEMA_NAME;

/***
 * execute 系列方法测试
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class LoadTest extends AbstractDbTest {
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
        try (Connection conn = DsUtils.mysqlConnection()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user")) {
                jdbcTemplate.executeUpdate("drop table tb_user");
            }
            //
            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
            jdbcTemplate.loadSQL("/net_hasor_db/tb_user_for_mysql.sql");
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
        }
    }

    @Test
    public void loadSQL_2() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConnection()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user")) {
                jdbcTemplate.executeUpdate("drop table tb_user");
            }
            //
            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
            InputStream asStream = ResourcesUtils.getResourceAsStream("/net_hasor_db/tb_user_for_mysql.sql");
            if (asStream == null) {
                assert false;
            }
            jdbcTemplate.loadSQL(new InputStreamReader(asStream));
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
        }
    }

    @Test
    public void loadSQL_3() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConnection()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user")) {
                jdbcTemplate.executeUpdate("drop table tb_user");
            }
            //
            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
            jdbcTemplate.loadSQL(StandardCharsets.UTF_8, "/net_hasor_db/tb_user_for_mysql.sql");
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
        }
    }

    @Test
    public void loadSplitSQL_1() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConnection()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user")) {
                jdbcTemplate.executeUpdate("drop table tb_user");
            }
            //
            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
            jdbcTemplate.loadSplitSQL(";", "/net_hasor_db/tb_user_for_mysql.sql");
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
        }
    }

    @Test
    public void loadSplitSQL_2() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConnection()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user")) {
                jdbcTemplate.executeUpdate("drop table tb_user");
            }
            //
            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
            jdbcTemplate.loadSplitSQL(";", StandardCharsets.UTF_8, "/net_hasor_db/tb_user_for_mysql.sql");
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
        }
    }

    @Test
    public void loadSplitSQL_3() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConnection()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            if (hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user")) {
                jdbcTemplate.executeUpdate("drop table tb_user");
            }
            //
            assert !hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
            InputStream asStream = ResourcesUtils.getResourceAsStream("/net_hasor_db/tb_user_for_mysql.sql");
            if (asStream == null) {
                assert false;
            }
            jdbcTemplate.loadSplitSQL(";", new InputStreamReader(asStream));
            assert hasTable(jdbcTemplate, null, MYSQL_SCHEMA_NAME, "tb_user");
        }
    }
}
