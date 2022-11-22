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
package net.hasor.test.utils;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

import static net.hasor.test.utils.TestUtils.*;

/***
 * 创建JDBC环境
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class DsUtils {
    public static String MYSQL_SCHEMA_NAME = "devtester";
    public static String MYSQL_JDBC_URL    = "jdbc:mysql://127.0.0.1:3306/devtester?allowMultiQueries=true";
    public static String PG_JDBC_URL       = "jdbc:postgresql://127.0.0.1:5432/postgres";
    public static String ORACLE_JDBC_URL   = "jdbc:oracle:thin:@127.0.0.1:1521:xe";

    private static void initH2(JdbcTemplate jdbcTemplate) throws SQLException {
        // init table
        jdbcTemplate.execute((ConnectionCallback<Object>) con -> {
            try {
                jdbcTemplate.executeUpdate("drop table tb_user");
            } catch (Exception ignored) {
            }
            try {
                jdbcTemplate.executeUpdate("drop table tb_h2types");
            } catch (Exception ignored) {
            }
            return null;
        });
        //
        try {
            jdbcTemplate.loadSQL("net_hasor_db/tb_user_for_h2.sql");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.loadSQL("net_hasor_db/all_types/tb_h2_types.sql");
        } catch (Exception ignored) {
        }
    }

    // Connection

    public static Connection h2Conn() throws SQLException {
        DefaultDs ds = new DefaultDs();
        ds.setUrl("jdbc:h2:mem:test_single");
        ds.setDriverClassName("org.h2.Driver");
        ds.setUsername("sa");
        ds.setPassword("");

        Connection conn = ds.getConnection();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
        initH2(jdbcTemplate);

        jdbcTemplate.executeUpdate("delete from tb_h2_types");
        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

        return conn;
    }

    public static Connection mysqlConn() throws SQLException {
        return DriverManager.getConnection(MYSQL_JDBC_URL, "root", "123456");
    }

    public static Connection oracleConn() throws SQLException {
        Connection connection = DriverManager.getConnection(ORACLE_JDBC_URL, "sys as sysdba", "oracle");
        connection.createStatement().execute("alter session set current_schema = SCOTT");
        return connection;
    }

    public static Connection pgConn() throws SQLException {
        return DriverManager.getConnection(PG_JDBC_URL, "postgres", "123456");
    }

    // DataSource

    public static DefaultDs h2Ds() throws Throwable {
        DefaultDs ds = new DefaultDs();
        ds.setUrl("jdbc:h2:mem:test_single");
        ds.setDriverClassName("org.h2.Driver");
        ds.setUsername("sa");
        ds.setPassword("");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        DsUtils.initH2(jdbcTemplate);
        jdbcTemplate.executeUpdate("delete from tb_h2_types");
        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

        return ds;
    }

    public static DefaultDs mysqlDs() throws SQLException {
        DefaultDs druid = new DefaultDs();
        druid.setUrl(MYSQL_JDBC_URL);
        druid.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druid.setUsername("root");
        druid.setPassword("123456");
        return druid;
    }

    public static DefaultDs oracleDs() throws SQLException {
        DefaultDs druid = new DefaultDs();
        druid.setUrl(ORACLE_JDBC_URL);
        druid.setUsername("sys as sysdba");
        druid.setPassword("oracle");
        druid.setConnectionInitSqls(Collections.singletonList("alter session set current_schema = SCOTT"));
        return druid;
    }

    public static DefaultDs pgDs() throws SQLException {
        DefaultDs druid = new DefaultDs();
        druid.setUrl(PG_JDBC_URL);
        druid.setUsername("postgres");
        druid.setPassword("123456");
        return druid;
    }
}
