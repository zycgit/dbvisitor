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
package net.hasor.dbvisitor.jdbc;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 动态获取 Connection。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-10
 */
public interface DynamicConnection {
    /** 获取数据库连接 */
    Connection getConnection() throws SQLException;

    /** 释放数据库连接 */
    void releaseConnection(Connection conn) throws SQLException;
}