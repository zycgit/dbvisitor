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

/***
 * 创建 JDBC
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class DsUtils {
    public static DataSource dsMySql(String url, String user, String password) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(100);
        dataSource.setMinimumIdle(1);
        return dataSource;
    }
}