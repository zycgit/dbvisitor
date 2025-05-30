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
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 用于处理 PreparedStatement 接口的动态参数设置。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-9
 */
@FunctionalInterface
public interface PreparedStatementSetter {
    /**
     * Set parameter values on the given PreparedStatement.
     * @param ps the PreparedStatement to invoke setter methods on
     * @throws SQLException if a SQLException is encountered (i.e. there is no need to catch SQLException)
     */
    void setValues(PreparedStatement ps) throws SQLException;
}