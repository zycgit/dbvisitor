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
package net.hasor.dbvisitor.lambda.core;
import net.hasor.dbvisitor.dialect.BoundSql;

import java.sql.SQLException;

/**
 * BoundSql 构建器接口
 *
 * 该函数式接口用于构建 BoundSql 对象，BoundSql 包含SQL语句和对应的参数信息。
 * 主要用于构建 SQL 时获取最终的 SQL 语句和参数。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-27
 */
@FunctionalInterface
public interface BoundSqlBuilder {
    /** 获取 BoundSql 对象 */
    BoundSql getBoundSql() throws SQLException;
}
