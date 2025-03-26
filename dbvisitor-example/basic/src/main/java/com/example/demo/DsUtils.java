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
package com.example.demo;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/***
 * 创建 JDBC
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class DsUtils {
    public static String MYSQL_JDBC_URL  = "jdbc:mysql://127.0.0.1:13306/devtester?allowMultiQueries=true";
    public static String PG_JDBC_URL     = "jdbc:postgresql://127.0.0.1:15432/postgres";
    public static String ORACLE_JDBC_URL = "jdbc:oracle:thin:@127.0.0.1:11521:xe";

    private static DataSource createDs(String driver, String url, String user, String password) throws SQLException {
        HikariDataSource druid = new HikariDataSource();
        druid.setJdbcUrl(url);
        druid.setDriverClassName(driver);
        druid.setUsername(user);
        druid.setPassword(password);
        druid.setMinimumIdle(5);
        druid.setMaximumPoolSize(12);
        druid.setMaxLifetime(1200000);
        druid.setAutoCommit(true);
        druid.setConnectionTimeout(20000);
        return druid;
    }

    public static Connection localMySql() throws SQLException {
        return DriverManager.getConnection(MYSQL_JDBC_URL, "root", "123456");
    }

    public static Connection localPg() throws SQLException {
        return DriverManager.getConnection(PG_JDBC_URL, "postgres", "123456");
    }

    public static Connection localOracle() throws SQLException {
        Connection connection = DriverManager.getConnection(ORACLE_JDBC_URL, "sys as sysdba", "oracle");
        connection.createStatement().execute("alter session set current_schema = SCOTT");
        return connection;
    }

    public static DataSource dsMySql() throws SQLException {
        return createDs("com.mysql.jdbc.Driver", MYSQL_JDBC_URL, "root", "123456");
    }

    public static DataSource dsPg() throws SQLException {
        return createDs("org.postgresql.Driver", PG_JDBC_URL, "postgres", "123456");
    }

    public static DataSource dsOracle() throws SQLException {
        return createDs("oracle.jdbc.driver.OracleDriver", ORACLE_JDBC_URL, "sys as sysdba", "oracle");
    }
}
