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
    public static String MYSQL_SCHEMA_NAME     = "devtester";
    public static String MYSQL_JDBC_URL        = "jdbc:mysql://127.0.0.1:3306/devtester?allowMultiQueries=true";
    //
    public static String ADB_MYSQL_SCHEMA_NAME = "adb_mysql_4387qyy";
    public static String ADB_MYSQL_JDBC_URL    = "jdbc:mysql://am-wz99xu17yks5p9e3f90650o.ads.aliyuncs.com:3306/adb_mysql_4387qyy";
    //
    public static String PG_JDBC_URL           = "jdbc:postgresql://127.0.0.1:5432/postgres";
    //
    public static String ORACLE_JDBC_URL       = "jdbc:oracle:thin:@127.0.0.1:1521:xe";

    private static DefaultDs createDs(String dbID) {
        DefaultDs druid = new DefaultDs();
        druid.setUrl("jdbc:h2:mem:test_" + dbID);
        druid.setDriverClassName("org.h2.Driver");
        druid.setUsername("sa");
        druid.setPassword("");
        return druid;
    }

    private static void initDB(JdbcTemplate jdbcTemplate) throws SQLException {
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

    public static Connection createConn() throws SQLException {
        Connection conn = DsUtils.createDs("single").getConnection();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
        DsUtils.initDB(jdbcTemplate);

        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
        jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

        return conn;
    }

    public static DefaultDs createDs() throws Throwable {
        return createDs(true);
    }

    public static DefaultDs createDs(boolean initData) throws Throwable {
        DefaultDs dataSource = DsUtils.createDs("single");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        DsUtils.initDB(jdbcTemplate);
        if (initData) {
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());
        }
        return dataSource;
    }

    public static Connection mysqlConnection() throws SQLException {
        return DriverManager.getConnection(MYSQL_JDBC_URL, "root", "123456");
    }

    public static DefaultDs mysqlDataSource() throws SQLException {
        DefaultDs druid = new DefaultDs();
        druid.setUrl(MYSQL_JDBC_URL);
        druid.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druid.setUsername("root");
        druid.setPassword("123456");
        return druid;
    }

    public static Connection pgConnection() throws SQLException {
        return DriverManager.getConnection(PG_JDBC_URL, "postgres", "123456");
    }

    public static DefaultDs pgDataSource() throws SQLException {
        DefaultDs druid = new DefaultDs();
        druid.setUrl(PG_JDBC_URL);
        druid.setUsername("postgres");
        druid.setPassword("123456");
        return druid;
    }

    public static Connection oracleConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(ORACLE_JDBC_URL, "sys as sysdba", "oracle");
        connection.createStatement().execute("alter session set current_schema = SCOTT");
        return connection;
    }

    public static DefaultDs oracleDataSource() throws SQLException {
        DefaultDs druid = new DefaultDs();
        druid.setUrl(ORACLE_JDBC_URL);
        druid.setUsername("sys as sysdba");
        druid.setPassword("oracle");
        druid.setConnectionInitSqls(Collections.singletonList("alter session set current_schema = SCOTT"));
        return druid;
    }
}
