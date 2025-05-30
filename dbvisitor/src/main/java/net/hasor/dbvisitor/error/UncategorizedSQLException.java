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
package net.hasor.dbvisitor.error;
import net.hasor.dbvisitor.jdbc.SqlProvider;

import java.sql.SQLException;

/**
 * 未分类的SQL执行异常，继承自 SQLException 并实现了 SqlProvider 接口
 * 用于封装JDBC操作过程中发生的异常，同时保留相关的SQL语句信息。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-14
 */
public class UncategorizedSQLException extends SQLException implements SqlProvider {
    private final String sql;

    /**
     * 构造方法 - 包含SQL语句、错误信息和原始异常
     * @param sql 导致异常的SQL语句
     * @param message 错误描述信息
     * @param ex 原始SQLException
     */
    public UncategorizedSQLException(String sql, String message, SQLException ex) {
        super(message, ex);
        this.sql = sql;
    }

    /**
     * @param sql 导致异常的SQL语句
     * @param message 错误描述信息
     */
    public UncategorizedSQLException(String sql, String message) {
        super(message);
        this.sql = sql;
    }

    /**
     * 获取导致异常的SQL语句
     * @return SQL语句字符串
     */
    @Override
    public String getSql() {
        return this.sql;
    }
}
