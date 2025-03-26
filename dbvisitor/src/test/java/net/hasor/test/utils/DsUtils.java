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
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

import static net.hasor.test.utils.TestUtils.*;

/***
 * 创建JDBC环境
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class DsUtils {
    public static String MYSQL_SCHEMA_NAME = "devtester";
    public static String MYSQL_JDBC_URL    = "jdbc:mysql://127.0.0.1:13306/devtester?allowMultiQueries=true";
    public static String PG_JDBC_URL       = "jdbc:postgresql://127.0.0.1:15432/postgres";
    public static String ORACLE_JDBC_URL   = "jdbc:oracle:thin:@127.0.0.1:11521:xe";

    private static void initH2(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.execute("drop all objects delete files;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_h2.sql");
            jdbcTemplate.loadSQL("dbvisitor_coverage/all_types/tb_h2_types.sql");
            jdbcTemplate.loadSQL("dbvisitor_coverage/auto_id_for_h2.sql");

            jdbcTemplate.loadSQL("/dbvisitor_scene/user_table_for_h2.sql");
            jdbcTemplate.execute("insert into user_table values (1, 'mali', 26, now());");
            jdbcTemplate.execute("insert into user_table values (2, 'dative', 32, now());");
            jdbcTemplate.execute("insert into user_table values (3, 'jon wes', 41, now());");
            jdbcTemplate.execute("insert into user_table values (4, 'mary', 66, now());");
            jdbcTemplate.execute("insert into user_table values (5, 'matt', 25, now());");

            jdbcTemplate.execute("create sequence test_seq;");
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private static void initMySql(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.execute("use information_schema;");
            jdbcTemplate.execute("drop database devtester;");
            jdbcTemplate.execute("create database devtester;");
            jdbcTemplate.execute("use devtester;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
            jdbcTemplate.loadSQL("dbvisitor_coverage/all_types/tb_mysql_types.sql");
            jdbcTemplate.loadSQL("dbvisitor_coverage/auto_id_for_mysql.sql");

            jdbcTemplate.loadSQL("/dbvisitor_scene/user_table_for_mysql.sql");
            jdbcTemplate.execute("insert into user_table values (1, 'mali', 26, now());");
            jdbcTemplate.execute("insert into user_table values (2, 'dative', 32, now());");
            jdbcTemplate.execute("insert into user_table values (3, 'jon wes', 41, now());");
            jdbcTemplate.execute("insert into user_table values (4, 'mary', 66, now());");
            jdbcTemplate.execute("insert into user_table values (5, 'matt', 25, now());");
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

        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

        return conn;
    }

    public static Connection mysqlConn() throws SQLException {
        Connection conn = DriverManager.getConnection(MYSQL_JDBC_URL, "root", "123456");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
        initMySql(jdbcTemplate);
        return conn;
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
