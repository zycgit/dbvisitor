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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 该接口用于创建 {@link CallableStatement} 对象。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-10-9
 */
@FunctionalInterface
public interface CallableStatementCreator {
    CallableStatement createCallableStatement(Connection con) throws SQLException;
}